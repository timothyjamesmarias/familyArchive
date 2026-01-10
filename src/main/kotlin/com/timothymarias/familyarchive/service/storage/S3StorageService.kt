package com.timothymarias.familyarchive.service.storage

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.InputStream
import java.time.Duration

@Service
@ConditionalOnProperty(name = ["storage.type"], havingValue = "s3")
class S3StorageService(
    private val storageProperties: StorageProperties,
    s3Client: S3Client? = null,
    s3Presigner: S3Presigner? = null
) : StorageService {

    private val logger = LoggerFactory.getLogger(S3StorageService::class.java)
    private val s3Client: S3Client
    private val s3Presigner: S3Presigner
    private val bucketName: String

    init {
        bucketName = storageProperties.s3.bucket

        // Use provided clients if available (for testing), otherwise create new ones
        this.s3Client = s3Client ?: run {
            val credentials = AwsBasicCredentials.create(
                storageProperties.s3.accessKey,
                storageProperties.s3.secretKey
            )

            S3Client.builder()
                .region(Region.of(storageProperties.s3.region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()
        }

        this.s3Presigner = s3Presigner ?: run {
            val credentials = AwsBasicCredentials.create(
                storageProperties.s3.accessKey,
                storageProperties.s3.secretKey
            )

            S3Presigner.builder()
                .region(Region.of(storageProperties.s3.region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()
        }

        logger.info("S3 storage initialized with bucket: $bucketName in region: ${storageProperties.s3.region}")
    }

    override fun store(file: MultipartFile, path: String): String {
        return store(
            file.inputStream,
            path,
            file.contentType ?: "application/octet-stream",
            file.size
        )
    }

    override fun store(inputStream: InputStream, path: String, contentType: String, contentLength: Long): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .contentType(contentType)
            .contentLength(contentLength)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength))

        logger.debug("Stored file in S3: s3://$bucketName/$path")
        return path
    }

    override fun retrieve(path: String): InputStream {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        return try {
            s3Client.getObject(getObjectRequest)
        } catch (e: NoSuchKeyException) {
            throw StorageException("File not found in S3: $path")
        }
    }

    override fun delete(path: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
        logger.debug("Deleted file from S3: s3://$bucketName/$path")
    }

    override fun exists(path: String): Boolean {
        return try {
            val headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build()
            s3Client.headObject(headObjectRequest)
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    override fun generateUrl(path: String): String {
        // If using CloudFront or public bucket, return the public URL
        // Otherwise, generate a long-lived presigned URL
        return if (storageProperties.s3.cloudFrontDomain.isNotBlank()) {
            "https://${storageProperties.s3.cloudFrontDomain}/$path"
        } else if (storageProperties.s3.publicBucket) {
            "https://$bucketName.s3.${storageProperties.s3.region}.amazonaws.com/$path"
        } else {
            // For private buckets, generate a presigned URL (default 1 hour)
            generatePresignedUrl(path, 3600)
        }
    }

    override fun generatePresignedUrl(path: String, expirationSeconds: Long): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(expirationSeconds))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)
        return presignedRequest.url().toString()
    }
}
