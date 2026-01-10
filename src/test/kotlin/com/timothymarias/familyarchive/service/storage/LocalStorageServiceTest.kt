package com.timothymarias.familyarchive.service.storage

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

class LocalStorageServiceTest {

    @TempDir
    private lateinit var tempDir: Path

    private lateinit var localStorageService: LocalStorageService
    private lateinit var storageProperties: StorageProperties

    @BeforeEach
    fun setup() {
        storageProperties = StorageProperties().apply {
            type = "local"
            local.uploadDir = tempDir.toString()
        }
        localStorageService = LocalStorageService(storageProperties)
    }

    @AfterEach
    fun cleanup() {
        // Cleanup is handled by @TempDir
    }

    @Test
    fun `should store and retrieve file using MultipartFile`() {
        // Given
        val content = "Hello, Local Storage!".toByteArray()
        val file = MockMultipartFile(
            "testfile",
            "test.txt",
            "text/plain",
            content
        )
        val path = "test/file.txt"

        // When
        val storedPath = localStorageService.store(file, path)

        // Then
        assertEquals(path, storedPath)
        assertTrue(localStorageService.exists(path))

        // Verify content
        val retrievedContent = localStorageService.retrieve(path).readAllBytes()
        assertArrayEquals(content, retrievedContent)
    }

    @Test
    fun `should store and retrieve file using InputStream`() {
        // Given
        val content = "Hello from InputStream!".toByteArray()
        val inputStream = ByteArrayInputStream(content)
        val path = "test/stream-file.txt"

        // When
        val storedPath = localStorageService.store(
            inputStream,
            path,
            "text/plain",
            content.size.toLong()
        )

        // Then
        assertEquals(path, storedPath)
        assertTrue(localStorageService.exists(path))

        // Verify content
        val retrievedContent = localStorageService.retrieve(path).readAllBytes()
        assertArrayEquals(content, retrievedContent)
    }

    @Test
    fun `should delete file`() {
        // Given
        val content = "File to be deleted".toByteArray()
        val file = MockMultipartFile("testfile", "delete.txt", "text/plain", content)
        val path = "test/delete-me.txt"
        localStorageService.store(file, path)

        // When
        localStorageService.delete(path)

        // Then
        assertFalse(localStorageService.exists(path))
    }

    @Test
    fun `should check if file exists`() {
        // Given
        val content = "Existence check".toByteArray()
        val file = MockMultipartFile("testfile", "exists.txt", "text/plain", content)
        val existingPath = "test/exists.txt"
        val nonExistingPath = "test/does-not-exist.txt"

        localStorageService.store(file, existingPath)

        // When/Then
        assertTrue(localStorageService.exists(existingPath))
        assertFalse(localStorageService.exists(nonExistingPath))
    }

    @Test
    fun `should generate URL for file`() {
        // Given
        val content = "URL generation test".toByteArray()
        val file = MockMultipartFile("testfile", "url.txt", "text/plain", content)
        val path = "test/url-file.txt"
        localStorageService.store(file, path)

        // When
        val url = localStorageService.generateUrl(path)

        // Then
        assertNotNull(url)
        assertEquals("/uploads/$path", url)
    }

    @Test
    fun `should generate presigned URL for file`() {
        // Given
        val content = "Presigned URL test".toByteArray()
        val file = MockMultipartFile("testfile", "presigned.txt", "text/plain", content)
        val path = "test/presigned-file.txt"
        localStorageService.store(file, path)

        // When
        val presignedUrl = localStorageService.generatePresignedUrl(path, 3600)

        // Then
        // For local storage, presigned URLs are the same as regular URLs
        assertNotNull(presignedUrl)
        assertEquals("/uploads/$path", presignedUrl)
    }

    @Test
    fun `should throw exception when retrieving non-existent file`() {
        // Given
        val nonExistentPath = "test/non-existent-file.txt"

        // When/Then
        assertThrows(StorageException::class.java) {
            localStorageService.retrieve(nonExistentPath)
        }
    }

    @Test
    fun `should handle binary content correctly`() {
        // Given - Binary content (simulating an image)
        val binaryContent = ByteArray(1024) { it.toByte() }
        val file = MockMultipartFile("image", "test.jpg", "image/jpeg", binaryContent)
        val path = "test/binary-file.jpg"

        // When
        localStorageService.store(file, path)

        // Then
        val retrievedContent = localStorageService.retrieve(path).readAllBytes()
        assertArrayEquals(binaryContent, retrievedContent)
        assertEquals(binaryContent.size, retrievedContent.size)
    }

    @Test
    fun `should create parent directories when storing file`() {
        // Given
        val content = "Nested path test".toByteArray()
        val file = MockMultipartFile("testfile", "nested.txt", "text/plain", content)
        val path = "deep/nested/path/file.txt"

        // When
        localStorageService.store(file, path)

        // Then
        assertTrue(localStorageService.exists(path))
        val fullPath = tempDir.resolve(path)
        assertTrue(Files.exists(fullPath.parent))
    }

    @Test
    fun `should overwrite existing file when storing with same path`() {
        // Given
        val initialContent = "Initial content".toByteArray()
        val updatedContent = "Updated content".toByteArray()
        val path = "test/overwrite.txt"

        localStorageService.store(
            MockMultipartFile("file", "test.txt", "text/plain", initialContent),
            path
        )

        // When
        localStorageService.store(
            MockMultipartFile("file", "test.txt", "text/plain", updatedContent),
            path
        )

        // Then
        val retrievedContent = localStorageService.retrieve(path).readAllBytes()
        assertArrayEquals(updatedContent, retrievedContent)
        assertFalse(retrievedContent.contentEquals(initialContent))
    }
}
