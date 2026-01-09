package com.timothymarias.familyarchive.service.storage

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface StorageService {
    /**
     * Store a file and return the storage path/key
     */
    fun store(file: MultipartFile, path: String): String

    /**
     * Store from an InputStream and return the storage path/key
     */
    fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String

    /**
     * Retrieve a file as an InputStream
     */
    fun retrieve(path: String): InputStream

    /**
     * Delete a file
     */
    fun delete(path: String)

    /**
     * Check if a file exists
     */
    fun exists(path: String): Boolean

    /**
     * Generate a publicly accessible URL for the file
     * For local storage, this might be a path relative to the app
     * For S3, this might be a presigned URL or CloudFront URL
     */
    fun generateUrl(path: String): String

    /**
     * Generate a presigned URL that expires after a duration (in seconds)
     * Useful for temporary access to private files
     */
    fun generatePresignedUrl(path: String, expirationSeconds: Long = 3600): String
}
