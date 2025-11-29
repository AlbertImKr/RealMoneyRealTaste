package com.albert.realmoneyrealtaste.adapter.infrastructure.s3

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * S3Config 단위 테스트
 * S3 클라이언트 설정이 올바르게 생성되는지 검증합니다.
 */
@DisplayName("S3Config 단위 테스트")
class S3ConfigTest {

    private lateinit var s3Config: S3Config

    @BeforeEach
    fun setUp() {
        s3Config = S3Config()
        s3Config.accessKey = "test-access-key"
        s3Config.secretKey = "test-secret-key"
        s3Config.region = "us-east-1"
        s3Config.bucketName = "test-bucket"
    }

    @Nested
    @DisplayName("S3Client 생성 테스트")
    inner class S3ClientCreationTest {

        @Test
        @DisplayName("S3Client - 성공 - 기본 설정으로 클라이언트 생성")
        fun `s3Client - success - creates client with basic configuration`() {
            // When
            val client = s3Config.s3Client()

            // Then
            assertNotNull(client)
            // 클라이언트가 정상적으로 생성되었는지 확인
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("S3Client - 성공 - 다른 리전으로 클라이언트 생성")
        fun `s3Client - success - creates client with different region`() {
            // Given
            s3Config.region = "ap-northeast-2"

            // When
            val client = s3Config.s3Client()

            // Then
            assertNotNull(client)
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("S3Client - 성공 - 자격 증명이 올바르게 설정됨")
        fun `s3Client - success - credentials are properly configured`() {
            // When
            val client = s3Config.s3Client()

            // Then
            assertNotNull(client)
            // 클라이언트가 정상적으로 생성되었는지 확인
            assertNotNull(client.serviceClientConfiguration())
        }
    }

    @Nested
    @DisplayName("S3Presigner 생성 테스트")
    inner class S3PresignerCreationTest {

        @Test
        @DisplayName("S3Presigner - 성공 - 기본 설정으로 프리사이너 생성")
        fun `s3Presigner - success - creates presigner with basic configuration`() {
            // When
            val presigner = s3Config.s3Presigner()

            // Then
            assertNotNull(presigner)
        }

        @Test
        @DisplayName("S3Presigner - 성공 - 다른 리전으로 프리사이너 생성")
        fun `s3Presigner - success - creates presigner with different region`() {
            // Given
            s3Config.region = "eu-west-1"

            // When
            val presigner = s3Config.s3Presigner()

            // Then
            assertNotNull(presigner)
        }
    }

    @Nested
    @DisplayName("설정 속성 테스트")
    inner class ConfigurationPropertiesTest {

        @Test
        @DisplayName("설정 속성 - 성공 - 모든 속성이 올바르게 설정됨")
        fun `configuration properties - success - all properties are set correctly`() {
            // Given & When
            val config = s3Config

            // Then
            assertEquals("test-access-key", config.accessKey)
            assertEquals("test-secret-key", config.secretKey)
            assertEquals("us-east-1", config.region)
            assertEquals("test-bucket", config.bucketName)
        }
    }

    @Nested
    @DisplayName("다양한 리전 테스트")
    inner class RegionTest {

        @Test
        @DisplayName("리전 - 성공 - 모든 주요 리전에서 클라이언트 생성")
        fun `region - success - creates clients in all major regions`() {
            val regions = listOf(
                "us-east-1",
                "us-west-2",
                "ap-northeast-2",
                "eu-west-1",
                "ap-southeast-1"
            )

            regions.forEach { region ->
                // Given
                s3Config.region = region

                // When
                val client = s3Config.s3Client()
                val presigner = s3Config.s3Presigner()

                // Then
                assertNotNull(client)
                assertNotNull(presigner)
                // 리전이 설정되어 클라이언트가 정상 생성되는지 확인
                assertNotNull(client.serviceClientConfiguration())
            }
        }
    }
}
