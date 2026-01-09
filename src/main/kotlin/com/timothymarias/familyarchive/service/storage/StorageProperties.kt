package com.timothymarias.familyarchive.service.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "storage")
class StorageProperties {
    var type: String = "local" // "local" or "s3"
    var local = LocalStorageProperties()
    var s3 = S3StorageProperties()
}

class LocalStorageProperties {
    var uploadDir: String = "uploads"
}

class S3StorageProperties {
    var bucket: String = ""
    var region: String = "us-east-1"
    var accessKey: String = ""
    var secretKey: String = ""
    var cloudFrontDomain: String = ""
    var publicBucket: Boolean = false
}
