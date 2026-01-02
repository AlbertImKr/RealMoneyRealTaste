package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Profile("prod")
@Configuration
@ConfigurationProperties(prefix = "oci.objectstorage")
class OciObjectStorageConfig {
    lateinit var accessKeyId: String
    lateinit var secretAccessKey: String
    lateinit var region: String
    lateinit var namespace: String
    lateinit var bucketName: String

    private val endpoint: String
        get() = "https://$namespace.compat.objectstorage.$region.oraclecloud.com"

    @Bean
    fun ociS3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)

        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .chunkedEncodingEnabled(false)
                    .build()
            )
            .overrideConfiguration(
                ClientOverrideConfiguration.builder()
                    .build()
            )
            .build()
    }

    @Bean
    fun ociS3Presigner(): S3Presigner {
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)

        return S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
    }
}
