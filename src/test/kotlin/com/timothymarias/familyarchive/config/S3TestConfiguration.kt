package com.timothymarias.familyarchive.config

import com.timothymarias.familyarchive.service.storage.S3StorageService
import com.timothymarias.familyarchive.service.storage.StorageProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

/**
 * Test configuration for S3 tests using Localstack.
 * This configuration sets up a LocalStack container and creates an S3 client configured to use it.
 */
@TestConfiguration
class S3TestConfiguration {

    companion object {
        private val localStackImage = DockerImageName.parse("localstack/localstack:3.0")

        val localstack: LocalStackContainer = LocalStackContainer(localStackImage)
            .withServices(LocalStackContainer.Service.S3)
            .apply {
                start()
            }
    }

    @Bean
    @Primary
    fun testStorageProperties(): StorageProperties {
        return StorageProperties().apply {
            type = "s3"
            s3.bucket = "test-bucket"
            s3.region = localstack.region
            s3.accessKey = localstack.accessKey
            s3.secretKey = localstack.secretKey
            s3.publicBucket = false
            s3.cloudFrontDomain = ""
        }
    }

    @Bean
    @Primary
    fun testS3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(
            localstack.accessKey,
            localstack.secretKey
        )

        return S3Client.builder()
            .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(localstack.region))
            .build()
    }

    @Bean
    @Primary
    fun testS3Presigner(): software.amazon.awssdk.services.s3.presigner.S3Presigner {
        val credentials = AwsBasicCredentials.create(
            localstack.accessKey,
            localstack.secretKey
        )

        return software.amazon.awssdk.services.s3.presigner.S3Presigner.builder()
            .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(localstack.region))
            .build()
    }

    @Bean
    @Primary
    fun testS3StorageService(
        storageProperties: StorageProperties,
        s3Client: S3Client,
        s3Presigner: software.amazon.awssdk.services.s3.presigner.S3Presigner
    ): S3StorageService {
        // Create the test bucket
        s3Client.createBucket { builder ->
            builder.bucket(storageProperties.s3.bucket)
        }

        return S3StorageService(storageProperties, s3Client, s3Presigner)
    }
}
