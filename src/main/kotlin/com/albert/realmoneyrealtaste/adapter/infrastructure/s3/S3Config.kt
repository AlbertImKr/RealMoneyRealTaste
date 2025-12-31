package com.albert.realmoneyrealtaste.adapter.infrastructure.s3

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Profile("aws")
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
class S3Config {
    lateinit var accessKey: String
    lateinit var secretKey: String
    lateinit var region: String
    lateinit var bucketName: String

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .region(Region.of(region)).build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        return S3Presigner.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .region(Region.of(region)).build()
    }
}
