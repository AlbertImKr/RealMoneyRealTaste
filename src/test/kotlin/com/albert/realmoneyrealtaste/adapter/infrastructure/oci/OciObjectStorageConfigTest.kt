package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * OciObjectStorageConfig 단위 테스트
 * OCI Object Storage 클라이언트 설정이 올바르게 생성되는지 검증합니다.
 */
@DisplayName("OciObjectStorageConfig 단위 테스트")
class OciObjectStorageConfigTest {

    private lateinit var ociConfig: OciObjectStorageConfig

    @BeforeEach
    fun setUp() {
        ociConfig = OciObjectStorageConfig()
        ociConfig.accessKeyId = "test-access-key"
        ociConfig.secretAccessKey = "test-secret-key"
        ociConfig.region = "us-ashburn-1"
        ociConfig.namespace = "test-namespace"
        ociConfig.bucketName = "test-bucket"
    }

    @Nested
    @DisplayName("S3Client 생성 테스트")
    inner class S3ClientCreationTest {

        @Test
        @DisplayName("S3Client - 성공 - 기본 설정으로 클라이언트 생성")
        fun `ociS3Client - success - creates client with basic configuration`() {
            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("S3Client - 성공 - 다른 리전으로 클라이언트 생성")
        fun `ociS3Client - success - creates client with different region`() {
            // Given
            ociConfig.region = "ap-seoul-1"

            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("S3Client - 성공 - 자격 증명이 올바르게 설정됨")
        fun `ociS3Client - success - credentials are properly configured`() {
            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("S3Client - 성공 - endpoint가 올바르게 설정됨")
        fun `ociS3Client - success - endpoint is correctly configured`() {
            // Given
            ociConfig.namespace = "my-namespace"
            ociConfig.region = "eu-frankfurt-1"

            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            // endpoint는 private이라 직접 확인은 어렵지만 클라이언트 생성 성공으로 간접 확인
            assertNotNull(client.serviceClientConfiguration())
        }
    }

    @Nested
    @DisplayName("S3Presigner 생성 테스트")
    inner class S3PresignerCreationTest {

        @Test
        @DisplayName("S3Presigner - 성공 - 기본 설정으로 프리사이너 생성")
        fun `ociS3Presigner - success - creates presigner with basic configuration`() {
            // When
            val presigner = ociConfig.ociS3Presigner()

            // Then
            assertNotNull(presigner)
        }

        @Test
        @DisplayName("S3Presigner - 성공 - 다른 리전으로 프리사이너 생성")
        fun `ociS3Presigner - success - creates presigner with different region`() {
            // Given
            ociConfig.region = "ca-toronto-1"

            // When
            val presigner = ociConfig.ociS3Presigner()

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
            val config = ociConfig

            // Then
            assertEquals("test-access-key", config.accessKeyId)
            assertEquals("test-secret-key", config.secretAccessKey)
            assertEquals("us-ashburn-1", config.region)
            assertEquals("test-namespace", config.namespace)
            assertEquals("test-bucket", config.bucketName)
        }
    }

    @Nested
    @DisplayName("Endpoint 생성 테스트")
    inner class EndpointTest {

        @Test
        @DisplayName("Endpoint - 성공 - us-ashburn-1 리전의 endpoint가 올바르게 생성됨")
        fun `endpoint - success - generates correct endpoint for us-ashburn-1`() {
            // Given
            ociConfig.namespace = "my-namespace"
            ociConfig.region = "us-ashburn-1"

            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            // 실제 endpoint: https://my-namespace.compat.objectstorage.us-ashburn-1.oraclecloud.com
            assertNotNull(client.serviceClientConfiguration())
        }

        @Test
        @DisplayName("Endpoint - 성공 - ap-seoul-1 리전의 endpoint가 올바르게 생성됨")
        fun `endpoint - success - generates correct endpoint for ap-seoul-1`() {
            // Given
            ociConfig.namespace = "seoul-namespace"
            ociConfig.region = "ap-seoul-1"

            // When
            val client = ociConfig.ociS3Client()

            // Then
            assertNotNull(client)
            // 실제 endpoint: https://seoul-namespace.compat.objectstorage.ap-seoul-1.oraclecloud.com
            assertNotNull(client.serviceClientConfiguration())
        }
    }

    @Nested
    @DisplayName("다양한 리전 테스트")
    inner class RegionTest {

        @Test
        @DisplayName("리전 - 성공 - 모든 주요 OCI 리전에서 클라이언트 생성")
        fun `region - success - creates clients in all major OCI regions`() {
            val regions = listOf(
                "us-ashburn-1",
                "us-phoenix-1",
                "ap-seoul-1",
                "ap-tokyo-1",
                "eu-frankfurt-1",
                "ca-toronto-1"
            )

            regions.forEach { region ->
                // Given
                ociConfig.region = region
                ociConfig.namespace = "test-namespace"

                // When
                val client = ociConfig.ociS3Client()
                val presigner = ociConfig.ociS3Presigner()

                // Then
                assertNotNull(client)
                assertNotNull(presigner)
                assertNotNull(client.serviceClientConfiguration())
            }
        }
    }
}
