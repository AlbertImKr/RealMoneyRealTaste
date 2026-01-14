package com.albert.realmoneyrealtaste.adapter.infrastructure.gcs

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("gcp")
@Configuration
@ConfigurationProperties(prefix = "gcp.storage")
class GcsConfig {
    lateinit var projectId: String
    lateinit var bucketName: String
    lateinit var region: String

    @Bean
    fun gcsStorageClient(): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .service
    }
}
