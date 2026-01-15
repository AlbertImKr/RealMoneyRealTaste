package com.albert.realmoneyrealtaste.adapter.infrastructure.gcs

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * GcsConfig 단위 테스트
 * GCS Storage 클라이언트 설정이 올바르게 생성되는지 검증합니다.
 */
class GcsConfigTest {

    private lateinit var gcsConfig: GcsConfig

    @BeforeEach
    fun setUp() {
        gcsConfig = GcsConfig()
        gcsConfig.projectId = "test-project-id"
        gcsConfig.bucketName = "test-bucket"
        gcsConfig.region = "us-central1"
    }

    @Nested
    inner class GcsStorageClientCreationTest {

        @Test
        fun `gcsStorageClient - success - creates client with basic configuration`() {
            // When
            val client = gcsConfig.gcsStorageClient()

            // Then
            assertNotNull(client)
        }

        @Test
        fun `gcsStorageClient - success - creates client with different project id`() {
            // Given
            gcsConfig.projectId = "my-project-123"

            // When
            val client = gcsConfig.gcsStorageClient()

            // Then
            assertNotNull(client)
        }

        @Test
        fun `gcsStorageClient - success - creates client with different region`() {
            // Given
            gcsConfig.region = "asia-northeast3"

            // When
            val client = gcsConfig.gcsStorageClient()

            // Then
            assertNotNull(client)
        }

        @Test
        fun `gcsStorageClient - success - creates client with complex project id`() {
            // Given
            gcsConfig.projectId = "my-gcp-project-2024"

            // When
            val client = gcsConfig.gcsStorageClient()

            // Then
            assertNotNull(client)
        }
    }

    @Nested
    inner class ConfigurationPropertiesTest {

        @Test
        fun `configuration properties - success - all properties are set correctly`() {
            // Given & When
            val config = gcsConfig

            // Then
            assertEquals("test-project-id", config.projectId)
            assertEquals("test-bucket", config.bucketName)
            assertEquals("us-central1", config.region)
        }

        @Test
        fun `configuration properties - success - properties can be updated`() {
            // Given
            gcsConfig.projectId = "new-project"
            gcsConfig.bucketName = "new-bucket"
            gcsConfig.region = "europe-west1"

            // When
            val config = gcsConfig

            // Then
            assertEquals("new-project", config.projectId)
            assertEquals("new-bucket", config.bucketName)
            assertEquals("europe-west1", config.region)
        }
    }

    @Nested
    inner class RegionTest {

        @Test
        fun `region - success - creates clients in all major GCP regions`() {
            val regions = listOf(
                "us-central1",
                "us-east1",
                "us-west1",
                "europe-west1",
                "europe-west2",
                "asia-northeast1",
                "asia-northeast3",
                "asia-southeast1",
                "australia-southeast1"
            )

            regions.forEach { region ->
                // Given
                gcsConfig.region = region
                gcsConfig.projectId = "test-project"

                // When
                val client = gcsConfig.gcsStorageClient()

                // Then
                assertNotNull(client)
            }
        }
    }

    @Nested
    inner class ProjectIdTest {

        @Test
        fun `project id - success - creates clients with various project id formats`() {
            val projectIds = listOf(
                "simple-project",
                "my-project-123",
                "my-project-12345",
                "project-with-dashes",
                "project_with_underscores",
                "a123456789012"
            )

            projectIds.forEach { projectId ->
                // Given
                gcsConfig.projectId = projectId
                gcsConfig.region = "us-central1"

                // When
                val client = gcsConfig.gcsStorageClient()

                // Then
                assertNotNull(client)
            }
        }
    }
}
