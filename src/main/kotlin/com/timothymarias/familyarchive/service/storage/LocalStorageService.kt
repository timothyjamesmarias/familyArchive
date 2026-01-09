package com.timothymarias.familyarchive.service.storage

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
@ConditionalOnProperty(name = ["storage.type"], havingValue = "local", matchIfMissing = true)
class LocalStorageService(
    private val storageProperties: StorageProperties
) : StorageService {

    private val logger = LoggerFactory.getLogger(LocalStorageService::class.java)
    private val uploadDir: Path

    init {
        uploadDir = Paths.get(storageProperties.local.uploadDir)
        try {
            Files.createDirectories(uploadDir)
            logger.info("Local storage initialized at: ${uploadDir.toAbsolutePath()}")
        } catch (e: Exception) {
            throw RuntimeException("Could not create upload directory!", e)
        }
    }

    override fun store(file: MultipartFile, path: String): String {
        val destinationFile = uploadDir.resolve(path)
        Files.createDirectories(destinationFile.parent)

        file.inputStream.use { inputStream ->
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        }

        logger.debug("Stored file locally: $path")
        return path
    }

    override fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String {
        val destinationFile = uploadDir.resolve(path)
        Files.createDirectories(destinationFile.parent)

        inputStream.use {
            Files.copy(it, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        }

        logger.debug("Stored file locally: $path")
        return path
    }

    override fun retrieve(path: String): InputStream {
        val file = uploadDir.resolve(path)
        if (!Files.exists(file)) {
            throw StorageException("File not found: $path")
        }
        return FileInputStream(file.toFile())
    }

    override fun delete(path: String) {
        val file = uploadDir.resolve(path)
        if (Files.exists(file)) {
            Files.delete(file)
            logger.debug("Deleted file locally: $path")
        }
    }

    override fun exists(path: String): Boolean {
        return Files.exists(uploadDir.resolve(path))
    }

    override fun generateUrl(path: String): String {
        // For local storage, return a path relative to the application
        // This will be served by a controller endpoint
        return "/uploads/$path"
    }

    override fun generatePresignedUrl(path: String, expirationSeconds: Long): String {
        // For local storage, presigned URLs don't really make sense
        // Just return the regular URL
        return generateUrl(path)
    }
}

class StorageException(message: String) : RuntimeException(message)
