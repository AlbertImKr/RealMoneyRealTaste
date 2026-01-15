package com.albert.realmoneyrealtaste.adapter.infrastructure.gcs

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.SignUrlOption
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GcsPresignedUrlGeneratorTest {

    private lateinit var storage: Storage
    private lateinit var gcsConfig: GcsConfig
    private lateinit var urlGenerator: GcsPresignedUrlGenerator

    @BeforeEach
    fun setUp() {
        storage = mockk()
        gcsConfig = GcsConfig()
        gcsConfig.bucketName = "test-bucket"

        urlGenerator = GcsPresignedUrlGenerator(
            storage = storage,
            gcsConfig = gcsConfig,
            uploadExpirationMinutes = 15,
            getExpirationMinutes = 60
        )
    }

    @Nested
    inner class PresignedPutUrlTest {

        @Test
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

            val mockUrl = URI.create("https://storage.googleapis.com/test-bucket/images/test-image.jpg").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(15L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

            // When
            val result = urlGenerator.generatePresignedPutUrl(imageKey, request)

            // Then
            assertNotNull(result)
            assertEquals(imageKey, result.key)
            assertEquals(
                "https://storage.googleapis.com/test-bucket/images/test-image.jpg",
                result.uploadUrl
            )
            assertEquals("test.jpg", result.metadata["original-name"])
            assertEquals("image/jpeg", result.metadata["content-type"])
            assertEquals("1024", result.metadata["file-size"])
            assertEquals("800", result.metadata["width"])
            assertEquals("600", result.metadata["height"])

            // Storage가 올바른 파라미터로 호출되었는지 검증
            verify {
                storage.signUrl(
                    match<BlobInfo> {
                        it.bucket == "test-bucket" &&
                            it.name == imageKey &&
                            it.contentType == "image/jpeg" &&
                            it.metadata?.get("original-name") == "test.jpg" &&
                            it.metadata?.get("file-size") == "1024" &&
                            it.metadata?.get("width") == "800" &&
                            it.metadata?.get("height") == "600"
                    },
                    eq(15L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            }
        }

        @Test
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

            val mockUrl = URI("https://storage.googleapis.com/test-bucket/images/test-image.png").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(15L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

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

            val mockUrl = URI.create("https://storage.googleapis.com/test-bucket/images/test-image.jpg").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(15L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

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
    inner class PresignedGetUrlTest {

        @Test
        fun `generatePresignedGetUrl - success - generates basic image download URL`() {
            // Given
            val imageKey = "images/test-image.jpg"

            val mockUrl = URI.create("https://storage.googleapis.com/test-bucket/images/test-image.jpg").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(60L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

            // When
            val result = urlGenerator.generatePresignedGetUrl(imageKey)

            // Then
            assertNotNull(result)
            assertEquals(
                "https://storage.googleapis.com/test-bucket/images/test-image.jpg",
                result
            )

            // Storage가 올바른 파라미터로 호출되었는지 검증
            verify {
                storage.signUrl(
                    match<BlobInfo> {
                        it.bucket == "test-bucket" &&
                            it.name == imageKey
                    },
                    eq(60L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            }
        }

        @Test
        fun `generatePresignedGetUrl - success - generates URL with different key`() {
            // Given
            val imageKey = "profile/user-avatar.png"

            val mockUrl = URI.create("https://storage.googleapis.com/test-bucket/profile/user-avatar.png").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(60L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

            // When
            val result = urlGenerator.generatePresignedGetUrl(imageKey)

            // Then
            assertNotNull(result)
            assertEquals(
                "https://storage.googleapis.com/test-bucket/profile/user-avatar.png",
                result
            )
        }
    }

    @Nested
    inner class ExpirationTimeTest {

        @Test
        fun `expiration time - success - generates URL with custom expiration time`() {
            // Given
            val customGenerator = GcsPresignedUrlGenerator(
                storage = storage,
                gcsConfig = gcsConfig,
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

            val mockUrl = URI.create("https://storage.googleapis.com/test-bucket/images/test-image.jpg").toURL()

            every {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(30L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            } returns mockUrl

            val beforeGeneration = Instant.now()

            // When
            val result = customGenerator.generatePresignedPutUrl(imageKey, request)

            val afterGeneration = Instant.now()

            // Then
            assertTrue(result.expiresAt.isAfter(beforeGeneration.minusSeconds(1)))
            assertTrue(result.expiresAt.isBefore(afterGeneration.plusSeconds(1800))) // 30분 = 1800초

            verify {
                storage.signUrl(
                    any<BlobInfo>(),
                    eq(30L),
                    eq(java.util.concurrent.TimeUnit.MINUTES),
                    any<SignUrlOption>(),
                    any<SignUrlOption>()
                )
            }
        }
    }
}
