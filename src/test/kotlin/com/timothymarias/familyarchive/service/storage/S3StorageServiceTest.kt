package com.timothymarias.familyarchive.service.storage

import com.timothymarias.familyarchive.config.S3TestConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.io.ByteArrayInputStream

@SpringBootTest
@Import(S3TestConfiguration::class)
@ActiveProfiles("test")
class S3StorageServiceTest {

    @Autowired
    private lateinit var s3StorageService: S3StorageService

    @Autowired
    private lateinit var storageProperties: StorageProperties

    @Autowired
    private lateinit var s3Client: S3Client

    @AfterEach
    fun cleanup() {
        // Clean up all objects in the bucket after each test
        val listRequest = ListObjectsV2Request.builder()
            .bucket(storageProperties.s3.bucket)
            .build()

        val objects = s3Client.listObjectsV2(listRequest).contents()
        objects.forEach { obj ->
            s3StorageService.delete(obj.key())
        }
    }

    @Test
    fun `should store and retrieve file using MultipartFile`() {
        // Given
        val content = "Hello, S3!".toByteArray()
        val file = MockMultipartFile(
            "testfile",
            "test.txt",
            "text/plain",
            content
        )
        val path = "test/file.txt"

        // When
        val storedPath = s3StorageService.store(file, path)

        // Then
        assertEquals(path, storedPath)
        assertTrue(s3StorageService.exists(path))

        // Verify content
        val retrievedContent = s3StorageService.retrieve(path).readAllBytes()
        assertArrayEquals(content, retrievedContent)
    }

    @Test
    fun `should store and retrieve file using InputStream`() {
        // Given
        val content = "Hello from InputStream!".toByteArray()
        val inputStream = ByteArrayInputStream(content)
        val path = "test/stream-file.txt"

        // When
        val storedPath = s3StorageService.store(
            inputStream,
            path,
            "text/plain",
            content.size.toLong()
        )

        // Then
        assertEquals(path, storedPath)
        assertTrue(s3StorageService.exists(path))

        // Verify content
        val retrievedContent = s3StorageService.retrieve(path).readAllBytes()
        assertArrayEquals(content, retrievedContent)
    }

    @Test
    fun `should delete file`() {
        // Given
        val content = "File to be deleted".toByteArray()
        val file = MockMultipartFile("testfile", "delete.txt", "text/plain", content)
        val path = "test/delete-me.txt"
        s3StorageService.store(file, path)

        // When
        s3StorageService.delete(path)

        // Then
        assertFalse(s3StorageService.exists(path))
    }

    @Test
    fun `should check if file exists`() {
        // Given
        val content = "Existence check".toByteArray()
        val file = MockMultipartFile("testfile", "exists.txt", "text/plain", content)
        val existingPath = "test/exists.txt"
        val nonExistingPath = "test/does-not-exist.txt"

        s3StorageService.store(file, existingPath)

        // When/Then
        assertTrue(s3StorageService.exists(existingPath))
        assertFalse(s3StorageService.exists(nonExistingPath))
    }

    @Test
    fun `should generate URL for file`() {
        // Given
        val content = "URL generation test".toByteArray()
        val file = MockMultipartFile("testfile", "url.txt", "text/plain", content)
        val path = "test/url-file.txt"
        s3StorageService.store(file, path)

        // When
        val url = s3StorageService.generateUrl(path)

        // Then
        assertNotNull(url)
        assertTrue(url.contains(path))
    }

    @Test
    fun `should generate presigned URL for file`() {
        // Given
        val content = "Presigned URL test".toByteArray()
        val file = MockMultipartFile("testfile", "presigned.txt", "text/plain", content)
        val path = "test/presigned-file.txt"
        s3StorageService.store(file, path)

        // When
        val presignedUrl = s3StorageService.generatePresignedUrl(path, 3600)

        // Then
        assertNotNull(presignedUrl)
        assertTrue(presignedUrl.contains(path))
        // Presigned URLs contain query parameters
        assertTrue(presignedUrl.contains("X-Amz-Algorithm") || presignedUrl.contains("?"))
    }

    @Test
    fun `should throw exception when retrieving non-existent file`() {
        // Given
        val nonExistentPath = "test/non-existent-file.txt"

        // When/Then
        assertThrows(StorageException::class.java) {
            s3StorageService.retrieve(nonExistentPath)
        }
    }

    @Test
    fun `should handle binary content correctly`() {
        // Given - Binary content (simulating an image)
        val binaryContent = ByteArray(1024) { it.toByte() }
        val file = MockMultipartFile("image", "test.jpg", "image/jpeg", binaryContent)
        val path = "test/binary-file.jpg"

        // When
        s3StorageService.store(file, path)

        // Then
        val retrievedContent = s3StorageService.retrieve(path).readAllBytes()
        assertArrayEquals(binaryContent, retrievedContent)
        assertEquals(binaryContent.size, retrievedContent.size)
    }
}
