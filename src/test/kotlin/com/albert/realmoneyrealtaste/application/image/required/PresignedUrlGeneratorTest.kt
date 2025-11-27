package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPostResponse
import com.albert.realmoneyrealtaste.domain.image.ImageType
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PresignedUrlGeneratorTest(
    val presignedUrlGenerator: PresignedUrlGenerator,
) : IntegrationTestBase() {

    @Test
    fun `generatePresignedPutUrl - success - generates valid presigned PUT URL`() {
        // Given
        val imageKey = "images/test.jpg"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 15L

        // When
        val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

        // Then
        assertNotNull(response.uploadUrl)
        assertTrue(response.uploadUrl.isNotEmpty())
        assertTrue(response.uploadUrl.contains(imageKey))

        assertEquals(imageKey, response.key)
        assertTrue(response.fields.isEmpty()) // PUT 방식에서는 fields가 비어있음

        // 만료 시간 확인
        val expectedMinExpiry = Instant.now().plus(Duration.ofMinutes(14))
        val expectedMaxExpiry = Instant.now().plus(Duration.ofMinutes(16))
        assertTrue(response.expiresAt.isAfter(expectedMinExpiry))
        assertTrue(response.expiresAt.isBefore(expectedMaxExpiry))
    }

    @Test
    fun `generatePresignedPutUrl - success - generates different URLs for different keys`() {
        // Given
        val key1 = "images/test1.jpg"
        val key2 = "images/test2.png"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 15L

        // When
        val response1 = presignedUrlGenerator.generatePresignedPutUrl(key1, request, expirationMinutes)
        val response2 = presignedUrlGenerator.generatePresignedPutUrl(key2, request, expirationMinutes)

        // Then
        assertTrue(response1.uploadUrl != response2.uploadUrl)
        assertEquals(key1, response1.key)
        assertEquals(key2, response2.key)
        assertTrue(response1.uploadUrl.contains(key1))
        assertTrue(response2.uploadUrl.contains(key2))
        assertTrue(response1.fields.isEmpty())
        assertTrue(response2.fields.isEmpty())
    }

    @Test
    fun `generatePresignedPutUrl - success - handles various content types`() {
        // Given
        val testCases = listOf(
            "image.jpg" to "image/jpeg",
            "photo.png" to "image/png",
            "graphic.webp" to "image/webp"
        )
        val expirationMinutes = 15L

        // When & Then
        testCases.forEach { (fileName, contentType) ->
            val request = ImageUploadRequest(
                fileName = fileName,
                fileSize = 1024L,
                contentType = contentType,
                width = 800,
                height = 600,
                imageType = ImageType.POST_IMAGE
            )
            val imageKey = "images/$fileName"

            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

            assertNotNull(response.uploadUrl)
            assertEquals(imageKey, response.key)
            assertTrue(response.fields.isEmpty())
            assertTrue(response.expiresAt.isAfter(Instant.now()))
        }
    }

    @Test
    fun `generatePresignedPutUrl - success - handles various image types`() {
        // Given
        val imageTypes = listOf(
            ImageType.POST_IMAGE,
            ImageType.PROFILE_IMAGE,
            ImageType.THUMBNAIL
        )
        val expirationMinutes = 15L

        // When & Then
        imageTypes.forEach { imageType ->
            val request = ImageUploadRequest(
                fileName = "test.jpg",
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 200,
                height = 200,
                imageType = imageType
            )
            val imageKey = "images/test.jpg"

            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

            assertNotNull(response.uploadUrl)
            assertEquals(imageKey, response.key)
            assertTrue(response.fields.isEmpty())
            assertTrue(response.expiresAt.isAfter(Instant.now()))
        }
    }

    @Test
    fun `generatePresignedPutUrl - success - handles various expiration times`() {
        // Given
        val imageKey = "images/test.jpg"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationTimes = listOf(1L, 5L, 15L, 30L, 60L, 120L)

        // When & Then
        expirationTimes.forEach { expirationMinutes ->
            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

            assertNotNull(response.uploadUrl)
            assertEquals(imageKey, response.key)
            assertTrue(response.fields.isEmpty())

            // 만료 시간 확인
            val expectedMinExpiry = Instant.now().plus(Duration.ofMinutes(expirationMinutes - 1))
            val expectedMaxExpiry = Instant.now().plus(Duration.ofMinutes(expirationMinutes + 1))
            assertTrue(response.expiresAt.isAfter(expectedMinExpiry))
            assertTrue(response.expiresAt.isBefore(expectedMaxExpiry))
        }
    }

    @Test
    fun `generatePresignedPutUrl - success - handles complex image keys`() {
        // Given
        val complexKeys = listOf(
            "images/2024/01/01/${UUID.randomUUID()}/test-image.jpg",
            "images/2024/12/31/${UUID.randomUUID()}/nested/path/image.png",
            "images/2024/06/15/${UUID.randomUUID()}/very/deep/nested/directory/structure/webp-image.webp"
        )
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 15L

        // When & Then
        complexKeys.forEach { imageKey ->
            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

            assertNotNull(response.uploadUrl)
            assertEquals(imageKey, response.key)
            assertTrue(response.uploadUrl.contains(imageKey))
            assertTrue(response.fields.isEmpty())
            assertTrue(response.expiresAt.isAfter(Instant.now()))
        }
    }

    @Test
    fun `generatePresignedPutUrl - success - handles boundary values`() {
        // Given - 최소값
        val minRequest = ImageUploadRequest(
            fileName = "a.jpg",
            fileSize = 1L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )
        val minImageKey = "images/a.jpg"
        val minExpiration = 1L

        // When
        val minResponse = presignedUrlGenerator.generatePresignedPutUrl(minImageKey, minRequest, minExpiration)

        // Then
        assertNotNull(minResponse.uploadUrl)
        assertEquals(minImageKey, minResponse.key)
        assertTrue(minResponse.fields.isEmpty())

        // Given - 최대값
        val maxRequest = ImageUploadRequest(
            fileName = "a".repeat(251) + ".jpg",
            fileSize = 5242880L,
            contentType = "image/jpeg",
            width = 2000,
            height = 2000,
            imageType = ImageType.POST_IMAGE
        )
        val maxImageKey = "images/" + "a".repeat(251) + ".jpg"
        val maxExpiration = 1440L // 24시간

        // When
        val maxResponse = presignedUrlGenerator.generatePresignedPutUrl(maxImageKey, maxRequest, maxExpiration)

        // Then
        assertNotNull(maxResponse.uploadUrl)
        assertEquals(maxImageKey, maxResponse.key)
        assertTrue(maxResponse.fields.isEmpty())
    }

    @Test
    fun `generatePresignedPutUrl - success - validates response structure`() {
        // Given
        val imageKey = "images/test.jpg"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 15L

        // When
        val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

        // Then
        // 필수 필드 검증
        assertNotNull(response.uploadUrl)
        assertTrue(response.uploadUrl.isNotBlank())

        assertNotNull(response.key)
        assertEquals(imageKey, response.key)

        assertNotNull(response.fields)
        assertTrue(response.fields.isEmpty()) // PUT 방식에서는 fields가 비어있음

        assertNotNull(response.expiresAt)
        assertTrue(response.expiresAt.isAfter(Instant.now()))
    }

    @Test
    fun `generatePresignedPutUrl - success - consistent expiration time`() {
        // Given
        val imageKey = "images/test.jpg"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 30L

        // When
        val response1 = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)
        val response2 = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

        // Then
        // 만료 시간은 유사해야 함 (초 단위 차이는 허용)
        val timeDifference = Duration.between(response1.expiresAt, response2.expiresAt).abs()
        assertTrue(timeDifference.seconds < 10) // 10초 이내 차이
    }

    @Test
    fun `PresignedUrlGenerator functional interface - lambda implementation`() {
        // Given
        val generatedResponses = mutableListOf<Triple<String, ImageUploadRequest, Long>>()
        val lambdaGenerator: PresignedUrlGenerator = PresignedUrlGenerator { imageKey, request, expirationMinutes ->
            generatedResponses.add(Triple(imageKey, request, expirationMinutes))
            PresignedPostResponse(
                uploadUrl = "https://lambda-generated.example.com/$imageKey",
                key = imageKey,
                fields = mapOf("X-Amz-Algorithm" to "AWS4-HMAC-SHA256"),
                expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes))
            )
        }

        val imageKey = "test/image.jpg"
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )
        val expirationMinutes = 15L

        // When
        val result = lambdaGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)

        // Then
        assertEquals("https://lambda-generated.example.com/test/image.jpg", result.uploadUrl)
        assertEquals(imageKey, result.key)
        assertEquals("AWS4-HMAC-SHA256", result.fields["X-Amz-Algorithm"])
        assertEquals(1, generatedResponses.size)
        assertEquals(Triple(imageKey, request, expirationMinutes), generatedResponses[0])
    }

    @Test
    fun `PresignedUrlGenerator functional interface - exception handling`() {
        // Given
        val exceptionGenerator = PresignedUrlGenerator { imageKey, request, expirationMinutes ->
            if (imageKey.contains("invalid")) {
                throw IllegalArgumentException("Invalid image key: $imageKey")
            }
            if (expirationMinutes > 60) {
                throw IllegalArgumentException("Expiration too long: $expirationMinutes minutes")
            }
            PresignedPostResponse(
                uploadUrl = "https://example.com/$imageKey",
                key = imageKey,
                fields = emptyMap(),
                expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes))
            )
        }

        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then - 성공 케이스
        val successResult = exceptionGenerator.generatePresignedPutUrl("valid/image.jpg", request, 30L)
        assertEquals("https://example.com/valid/image.jpg", successResult.uploadUrl)

        // When & Then - 실패 케이스 (잘못된 키)
        try {
            exceptionGenerator.generatePresignedPutUrl("invalid/image.jpg", request, 30L)
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid image key: invalid/image.jpg", e.message)
        }

        // When & Then - 실패 케이스 (만료 시간 너무 김)
        try {
            exceptionGenerator.generatePresignedPutUrl("valid/image.jpg", request, 120L)
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            assertEquals("Expiration too long: 120 minutes", e.message)
        }
    }

    @Test
    fun `PresignedUrlGenerator functional interface - multiple calls`() {
        // Given
        val callCount = mutableListOf<Triple<String, ImageUploadRequest, Long>>()
        val countingGenerator: PresignedUrlGenerator = PresignedUrlGenerator { imageKey, request, expirationMinutes ->
            callCount.add(Triple(imageKey, request, expirationMinutes))
            PresignedPostResponse(
                uploadUrl = "https://counting.example.com/$imageKey",
                key = imageKey,
                fields = emptyMap(),
                expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes))
            )
        }

        val testCases = listOf(
            Triple(
                "image1.jpg",
                ImageUploadRequest("test1.jpg", 1024L, "image/jpeg", 800, 600, ImageType.POST_IMAGE),
                15L
            ),
            Triple(
                "image2.png",
                ImageUploadRequest("test2.png", 2048L, "image/png", 400, 400, ImageType.PROFILE_IMAGE),
                30L
            ),
            Triple(
                "image3.webp",
                ImageUploadRequest("test3.webp", 512L, "image/webp", 50, 50, ImageType.THUMBNAIL),
                60L
            )
        )

        // When
        val results = testCases.map { (imageKey, request, expirationMinutes) ->
            countingGenerator.generatePresignedPutUrl(imageKey, request, expirationMinutes)
        }

        // Then
        assertEquals(3, results.size)
        assertEquals(3, callCount.size)

        results.zip(testCases) { result, testCase ->
            val (imageKey, _, _) = testCase
            assertEquals("https://counting.example.com/$imageKey", result.uploadUrl)
            assertEquals(imageKey, result.key)
            assertTrue(result.fields.isEmpty())
        }

        assertEquals(testCases, callCount)
    }

    // 테스트용 보조 클래스
    private class TestPresignedUrlGenerator {
        private val generatedRequests = mutableSetOf<Triple<String, ImageUploadRequest, Long>>()

        fun generatePresignedPutUrl(
            imageKey: String,
            request: ImageUploadRequest,
            expirationMinutes: Long,
        ): PresignedPostResponse {
            generatedRequests.add(Triple(imageKey, request, expirationMinutes))
            return PresignedPostResponse(
                uploadUrl = "method-ref-test/$imageKey",
                key = imageKey,
                fields = emptyMap(),
                expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes))
            )
        }

        fun isGenerated(imageKey: String, request: ImageUploadRequest, expirationMinutes: Long): Boolean {
            return generatedRequests.contains(Triple(imageKey, request, expirationMinutes))
        }
    }
}
