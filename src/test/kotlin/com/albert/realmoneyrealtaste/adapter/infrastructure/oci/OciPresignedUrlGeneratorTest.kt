package com.albert.realmoneyrealtaste.adapter.infrastructure.oci

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.domain.image.ImageType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OciPresignedUrlGeneratorTest {

    private lateinit var s3Presigner: S3Presigner
    private lateinit var ociConfig: OciObjectStorageConfig
    private lateinit var urlGenerator: OciPresignedUrlGenerator

    @BeforeEach
    fun setUp() {
        s3Presigner = mockk()
        ociConfig = OciObjectStorageConfig()
        ociConfig.bucketName = "test-bucket"

        urlGenerator = OciPresignedUrlGenerator(
            s3Presigner = s3Presigner,
            ociConfig = ociConfig,
            uploadExpirationMinutes = 15,
            getExpirationMinutes = 60
        )
    }

    @Nested
    @DisplayName("Presigned PUT URL 생성 테스트")
    inner class PresignedPutUrlTest {

        @Test
        @DisplayName("generatePresignedPutUrl - 성공 - 기본 이미지 업로드 URL 생성")
        fun `generatePresignedPutUrl - success - generates basic image upload URL`() {
            // Given
            val imageKey = "images/test-image.jpg"
            val request = ImageUploadRequest(
                contentType = "image/jpeg",
                fileName = "test.jpg",
                fileSize = 1024L,
                width = 800,
                height = 600,
                imageType = ImageType.PROFILE_IMAGE,
            )

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg")
                .toURL()

            every {
                s3Presigner.presignPutObject(any<PutObjectPresignRequest>())
            } returns mockPresignedRequest

            // When
            val result = urlGenerator.generatePresignedPutUrl(imageKey, request)

            // Then
            assertNotNull(result)
            assertEquals(imageKey, result.key)
            assertEquals(
                "https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg",
                result.uploadUrl
            )
            assertEquals("test.jpg", result.metadata["original-name"])
            assertEquals("image/jpeg", result.metadata["content-type"])
            assertEquals("1024", result.metadata["file-size"])
            assertEquals("800", result.metadata["width"])
            assertEquals("600", result.metadata["height"])

            // S3Presigner가 올바른 파라미터로 호출되었는지 검증
            verify {
                s3Presigner.presignPutObject(
                    match<PutObjectPresignRequest> {
                        it.putObjectRequest().bucket() == "test-bucket" &&
                            it.putObjectRequest().key() == imageKey &&
                            it.putObjectRequest().contentType() == "image/jpeg" &&
                            it.putObjectRequest().metadata()["original-name"] == "test.jpg" &&
                            it.putObjectRequest().metadata()["file-size"] == "1024" &&
                            it.putObjectRequest().metadata()["width"] == "800" &&
                            it.putObjectRequest().metadata()["height"] == "600"
                    }
                )
            }
        }

        @Test
        @DisplayName("generatePresignedPutUrl - 성공 - PNG 이미지 업로드 URL 생성")
        fun `generatePresignedPutUrl - success - generates PNG image upload URL`() {
            // Given
            val imageKey = "images/test-image.png"
            val request = ImageUploadRequest(
                contentType = "image/png",
                fileName = "test.png",
                fileSize = 2048L,
                width = 1024,
                height = 768,
                imageType = ImageType.PROFILE_IMAGE,
            )

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.png")
                .toURL()

            every {
                s3Presigner.presignPutObject(any<PutObjectPresignRequest>())
            } returns mockPresignedRequest

            // When
            val result = urlGenerator.generatePresignedPutUrl(imageKey, request)

            // Then
            assertNotNull(result)
            assertEquals(imageKey, result.key)
            assertEquals("image/png", result.metadata["content-type"])
            assertEquals("2048", result.metadata["file-size"])
            assertEquals("1024", result.metadata["width"])
            assertEquals("768", result.metadata["height"])
        }

        @Test
        @DisplayName("generatePresignedPutUrl - 성공 - 만료 시간이 올바르게 설정됨")
        fun `generatePresignedPutUrl - success - expiration time is correctly set`() {
            // Given
            val imageKey = "images/test-image.jpg"
            val request = ImageUploadRequest(
                contentType = "image/jpeg",
                fileName = "test.jpg",
                fileSize = 1024L,
                width = 800,
                height = 600,
                imageType = ImageType.PROFILE_IMAGE,
            )

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg")
                .toURL()

            every {
                s3Presigner.presignPutObject(any<PutObjectPresignRequest>())
            } returns mockPresignedRequest

            val beforeGeneration = Instant.now()

            // When
            val result = urlGenerator.generatePresignedPutUrl(imageKey, request)

            val afterGeneration = Instant.now()

            // Then
            assertTrue(result.expiresAt.isAfter(beforeGeneration.minusSeconds(1)))
            assertTrue(result.expiresAt.isBefore(afterGeneration.plusSeconds(900))) // 15분 = 900초
        }
    }

    @Nested
    @DisplayName("Presigned GET URL 생성 테스트")
    inner class PresignedGetUrlTest {

        @Test
        @DisplayName("generatePresignedGetUrl - 성공 - 기본 이미지 다운로드 URL 생성")
        fun `generatePresignedGetUrl - success - generates basic image download URL`() {
            // Given
            val imageKey = "images/test-image.jpg"

            val mockPresignedRequest = mockk<PresignedGetObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg")
                .toURL()

            every {
                s3Presigner.presignGetObject(any<GetObjectPresignRequest>())
            } returns mockPresignedRequest

            // When
            val result = urlGenerator.generatePresignedGetUrl(imageKey)

            // Then
            assertNotNull(result)
            assertEquals(
                "https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg",
                result
            )

            // S3Presigner가 올바른 파라미터로 호출되었는지 검증
            verify {
                s3Presigner.presignGetObject(
                    match<GetObjectPresignRequest> {
                        it.getObjectRequest().bucket() == "test-bucket" &&
                            it.getObjectRequest().key() == imageKey
                    }
                )
            }
        }

        @Test
        @DisplayName("generatePresignedGetUrl - 성공 - 다른 키로 URL 생성")
        fun `generatePresignedGetUrl - success - generates URL with different key`() {
            // Given
            val imageKey = "profile/user-avatar.png"

            val mockPresignedRequest = mockk<PresignedGetObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/profile/user-avatar.png")
                .toURL()

            every {
                s3Presigner.presignGetObject(any<GetObjectPresignRequest>())
            } returns mockPresignedRequest

            // When
            val result = urlGenerator.generatePresignedGetUrl(imageKey)

            // Then
            assertNotNull(result)
            assertEquals(
                "https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/profile/user-avatar.png",
                result
            )
        }
    }

    @Nested
    @DisplayName("만료 시간 설정 테스트")
    inner class ExpirationTimeTest {

        @Test
        @DisplayName("만료 시간 - 성공 - 커스텀 만료 시간으로 URL 생성")
        fun `expiration time - success - generates URL with custom expiration time`() {
            // Given
            val customGenerator = OciPresignedUrlGenerator(
                s3Presigner = s3Presigner,
                ociConfig = ociConfig,
                uploadExpirationMinutes = 30,
                getExpirationMinutes = 120
            )

            val imageKey = "images/test-image.jpg"
            val request = ImageUploadRequest(
                contentType = "image/jpeg",
                fileName = "test.jpg",
                fileSize = 1024L,
                width = 800,
                height = 600,
                imageType = ImageType.PROFILE_IMAGE,
            )

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every { mockPresignedRequest.url() } returns URI.create("https://test-bucket.compat.objectstorage.us-ashburn-1.oraclecloud.com/images/test-image.jpg")
                .toURL()

            every {
                s3Presigner.presignPutObject(any<PutObjectPresignRequest>())
            } returns mockPresignedRequest

            val beforeGeneration = Instant.now()

            // When
            val result = customGenerator.generatePresignedPutUrl(imageKey, request)

            val afterGeneration = Instant.now()

            // Then
            assertTrue(result.expiresAt.isAfter(beforeGeneration.minusSeconds(1)))
            assertTrue(result.expiresAt.isBefore(afterGeneration.plusSeconds(1800))) // 30분 = 1800초
        }
    }
}
