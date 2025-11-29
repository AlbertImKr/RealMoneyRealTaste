package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
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
        val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

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
        val response1 = presignedUrlGenerator.generatePresignedPutUrl(key1, request)
        val response2 = presignedUrlGenerator.generatePresignedPutUrl(key2, request)

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

            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

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

            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

            assertNotNull(response.uploadUrl)
            assertEquals(imageKey, response.key)
            assertTrue(response.fields.isEmpty())
            assertTrue(response.expiresAt.isAfter(Instant.now()))
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
            val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

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
        val minResponse = presignedUrlGenerator.generatePresignedPutUrl(minImageKey, minRequest)

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
        val maxResponse = presignedUrlGenerator.generatePresignedPutUrl(maxImageKey, maxRequest)

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
        val response = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

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
        val response1 = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)
        val response2 = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

        // Then
        // 만료 시간은 유사해야 함 (초 단위 차이는 허용)
        val timeDifference = Duration.between(response1.expiresAt, response2.expiresAt).abs()
        assertTrue(timeDifference.seconds < 10) // 10초 이내 차이
    }
}
