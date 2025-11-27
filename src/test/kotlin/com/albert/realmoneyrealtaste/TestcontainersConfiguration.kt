package com.albert.realmoneyrealtaste

import com.albert.realmoneyrealtaste.adapter.infrastructure.s3.S3Config
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        return MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .apply {
                withDatabaseName("testdb")
                withUsername("testuser")
                withPassword("testpass")

                // GitHub Actions 환경 최적화
                withReuse(true)
                withStartupTimeout(Duration.ofMinutes(5)) // 시간 여유

                // 메모리 및 성능 최적화
                withCommand(
                    "--character-set-server=utf8mb4",
                    "--innodb-flush-log-at-trx-commit=0",
                    "--innodb-flush-method=O_DIRECT_NO_FSYNC",
                    "--innodb-buffer-pool-size=64M", // 메모리 절약
                    "--skip-log-bin",
                    "--innodb-doublewrite=0", // 성능 향상
                    "--sync-binlog=0"
                )

                // CI 환경에서는 파일시스템 바인드 제거 (권한 이슈 방지)
                if (!System.getenv("CI").isNullOrEmpty()) {
                    // CI 환경에서는 바인드 마운트 사용 안함
                } else {
                    withFileSystemBind(
                        "/tmp/mysql-data",
                        "/var/lib/mysql",
                        org.testcontainers.containers.BindMode.READ_WRITE
                    )
                }

                // 헬스체크 설정
                waitingFor(
                    Wait.forLogMessage(".*ready for connections.*", 2)
                        .withStartupTimeout(Duration.ofMinutes(3))
                )
            }
    }

    @Bean
    fun localStackContainer(): LocalStackContainer {
        return LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .apply {
                withServices(LocalStackContainer.Service.S3)
                withReuse(true)
            }
    }

    @Bean
    @Primary
    fun testS3Config(localStackContainer: LocalStackContainer): S3Config {
        return S3Config().apply {
            accessKey = localStackContainer.accessKey
            secretKey = localStackContainer.secretKey
            region = localStackContainer.region
            bucketName = "test-bucket"
        }
    }

    @Bean
    @Primary
    fun testS3Client(testS3Config: S3Config): S3Client {
        val s3Client = S3Client.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(testS3Config.accessKey, testS3Config.secretKey)
                )
            )
            .region(Region.of(testS3Config.region))
            .build()

        // 테스트용 버킷 생성
        createTestBucket(s3Client, testS3Config.bucketName)

        return s3Client
    }

    @Bean
    @Primary
    fun testS3Presigner(testS3Config: S3Config): S3Presigner {
        return S3Presigner.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(testS3Config.accessKey, testS3Config.secretKey)
                )
            )
            .region(Region.of(testS3Config.region))
            .build()
    }

    private fun createTestBucket(s3Client: S3Client, bucketName: String) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
        } catch (e: Exception) {
            // 버킷이 이미 존재하면 무시
        }
    }
}
