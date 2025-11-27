package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.exception.ImageGenerateException
import com.albert.realmoneyrealtaste.domain.image.ImageType
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageUploadRequesterTest(
    val imageUploadRequester: ImageUploadRequester,
) : IntegrationTestBase() {

    @Test
    fun `generatePresignedPostUrl - success - generates valid presigned URL for valid request`() {
        // Given
        val userId = 123L
        val request = ImageUploadRequest(
            fileName = "test-image.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val response = imageUploadRequester.generatePresignedPostUrl(request, userId)

        // Then
        assertNotNull(response.uploadUrl)
        assertTrue(response.uploadUrl.isNotEmpty())
        assertTrue(response.uploadUrl.contains("amazonaws.com") || response.uploadUrl.contains("s3"))

        assertNotNull(response.key)
        assertTrue(response.key.isNotEmpty())
        assertTrue(response.key.contains("images/"))
        assertTrue(response.key.endsWith(".jpg"))

        assertNotNull(response.fields)
        assertTrue(response.fields.isEmpty())

        // 만료 시간 확인 (기본 15분 후)
        val expectedMinExpiry = Instant.now().plus(Duration.ofMinutes(14))
        val expectedMaxExpiry = Instant.now().plus(Duration.ofMinutes(16))
        assertTrue(response.expiresAt.isAfter(expectedMinExpiry))
        assertTrue(response.expiresAt.isBefore(expectedMaxExpiry))
    }

    @Test
    fun `generatePresignedPostUrl - success - generates different URLs for same request`() {
        // Given
        val userId = 123L
        val request = ImageUploadRequest(
            fileName = "test-image.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val response1 = imageUploadRequester.generatePresignedPostUrl(request, userId)
        val response2 = imageUploadRequester.generatePresignedPostUrl(request, userId)

        // Then
        // URL은 다르지만 키 형식은 유사해야 함
        assert(response1.uploadUrl != response2.uploadUrl)
        assert(response1.key != response2.key) // UUID로 인해 다름

        // 만료 시간은 비슷해야 함
        val timeDifference = Duration.between(response1.expiresAt, response2.expiresAt).abs()
        assertTrue(timeDifference.seconds < 60) // 1분 이내 차이
    }

    @Test
    fun `generatePresignedPostUrl - success - handles various image types`() {
        // Given
        val userId = 123L
        val imageTypes = listOf(
            ImageType.POST_IMAGE,
            ImageType.PROFILE_IMAGE,
            ImageType.THUMBNAIL
        )

        // When & Then
        imageTypes.forEach { imageType ->
            val request = ImageUploadRequest(
                fileName = "test-image.jpg",
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 100,
                height = 100,
                imageType = imageType
            )

            val response = imageUploadRequester.generatePresignedPostUrl(request, userId)

            assertNotNull(response.uploadUrl)
            assertNotNull(response.key)
            assertTrue(response.fields.isEmpty())
            assertTrue(response.expiresAt.isAfter(Instant.now()))
        }
    }

    @Test
    fun `generatePresignedPostUrl - success - handles various content types`() {
        // Given
        val userId = 123L
        val testCases = listOf(
            "image.jpg" to "image/jpeg",
            "photo.png" to "image/png",
            "graphic.webp" to "image/webp"
        )

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

            val response = imageUploadRequester.generatePresignedPostUrl(request, userId)

            assertNotNull(response.uploadUrl)
            assertTrue(response.key.endsWith(fileName.substringAfterLast(".")))
        }
    }

    @Test
    fun `generatePresignedPostUrl - success - handles boundary values`() {
        // Given
        val userId = 123L

        // 최소값
        val minRequest = ImageUploadRequest(
            fileName = "a.jpg",
            fileSize = 1L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val minResponse = imageUploadRequester.generatePresignedPostUrl(minRequest, userId)

        // Then
        assertNotNull(minResponse.uploadUrl)
        assertNotNull(minResponse.key)
        assertTrue(minResponse.key.endsWith(".jpg"))

        // Given - 최대값
        val maxRequest = ImageUploadRequest(
            fileName = "a".repeat(251) + ".jpg",
            fileSize = 5242880L, // 5MB
            contentType = "image/jpeg",
            width = 2000,
            height = 2000,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val maxResponse = imageUploadRequester.generatePresignedPostUrl(maxRequest, userId)

        // Then
        assertNotNull(maxResponse.uploadUrl)
        assertNotNull(maxResponse.key)
        assertTrue(maxResponse.key.endsWith(".jpg"))
    }

    @Test
    fun `generatePresignedPostUrl - success - generates secure key format`() {
        // Given
        val userId = 123L
        val request = ImageUploadRequest(
            fileName = "special.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val response = imageUploadRequester.generatePresignedPostUrl(request, userId)

        // Then
        // 키 형식: images/yyyy/MM/dd/uuid.extension
        assertTrue(response.key.startsWith("images/"))
        assertTrue(response.key.contains("/"))

        val parts = response.key.split("/")
        assertEquals(5, parts.size) // images, yyyy, MM, dd, uuid.extension
        assertEquals("images", parts[0])

        // UUID 부분 검증
        val uuidPart = parts.last().substringBeforeLast(".")
        assertEquals(36, uuidPart.length)
        assertTrue(uuidPart.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))

        // 확장자 검증
        assertTrue(response.key.endsWith(".jpg"))
    }

    @Test
    fun `generatePresignedPostUrl - success - boundary user IDs`() {
        // Given - 최소 사용자 ID
        val minUserId = 1L
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val minResponse = imageUploadRequester.generatePresignedPostUrl(request, minUserId)

        // Then
        assertNotNull(minResponse.uploadUrl)
        assertNotNull(minResponse.key)

        // Given - 최대 사용자 ID
        val maxUserId = Long.MAX_VALUE
        val maxResponse = imageUploadRequester.generatePresignedPostUrl(request, maxUserId)

        // Then
        assertNotNull(maxResponse.uploadUrl)
        assertNotNull(maxResponse.key)
    }

    @Test
    fun `generatePresignedPostUrl - success - validates response structure`() {
        // Given
        val userId = 123L
        val request = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When
        val response = imageUploadRequester.generatePresignedPostUrl(request, userId)

        // Then - 필수 필드 검증
        assertNotNull(response.uploadUrl)

        assertNotNull(response.key)
        assertTrue(response.key.isNotBlank())

        assertNotNull(response.fields)
        assertTrue(response.fields.isEmpty())

        // 만료 시간 검증
        assertTrue(response.expiresAt.isAfter(Instant.now()))
        assertTrue(response.expiresAt.isAfter(Instant.now().plus(Duration.ofMinutes(10))))
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for invalid file size`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 0L, // 잘못된 파일 크기
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for invalid content type`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test.txt",
            fileSize = 1024L,
            contentType = "text/plain", // 지원하지 않는 콘텐츠 타입
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for invalid dimensions`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 50, // 최소 크기보다 작음
            height = 50,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for invalid file name`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "", // 빈 파일명
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for oversized file`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 10_000_000L, // 10MB (5MB 제한 초과)
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for invalid image type dimensions`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "profile.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 50, // 프로필 이미지 최소 크기(100)보다 작음
            height = 50,
            imageType = ImageType.PROFILE_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for thumbnail oversized dimensions`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "thumb.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 200, // 썸네일 최대 크기(100) 초과
            height = 200,
            imageType = ImageType.THUMBNAIL
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for unsupported file extension`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test.gif", // 지원하지 않는 확장자
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for special characters in file name`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "test@image.jpg", // 특수문자 포함
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for path traversal attempt`() {
        // Given
        val userId = 123L
        val invalidRequest = ImageUploadRequest(
            fileName = "../../test.jpg", // Path Traversal 시도
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(invalidRequest, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `generatePresignedPostUrl - failure - throws ImageGenerateException for wrong name`() {
        // Given
        val userId = 1L
        val request = ImageUploadRequest(
            fileName = "test^.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        val exception = assertFailsWith<ImageGenerateException> {
            imageUploadRequester.generatePresignedPostUrl(request, userId)
        }
        assertEquals("이미지 업로드 실패", exception.message)
        assertNotNull(exception.cause)
    }

    @Test
    fun `confirmUpload - success - confirms upload and creates image record`() {
        // Given
        val userId = 123L
        val imageKey = "images/2024/01/01/${java.util.UUID.randomUUID()}.jpg"

        // When
        val result = (imageUploadRequester as ImageUploadTracker).confirmUpload(imageKey, userId)

        // Then
        assertTrue(result.success)
        assertEquals(imageKey, result.key)
        assertNotNull(result.url)
        assertTrue(result.url.isNotEmpty())
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for invalid key format`() {
        // Given
        val userId = 123L
        val invalidKey = "" // 빈 키

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(invalidKey, userId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for negative user ID`() {
        // Given
        val negativeUserId = -1L
        val imageKey = "images/2024/01/01/test.jpg"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(imageKey, negativeUserId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for zero user ID`() {
        // Given
        val zeroUserId = 0L
        val imageKey = "images/2024/01/01/test.jpg"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(imageKey, zeroUserId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for malformed key`() {
        // Given
        val userId = 123L
        val malformedKey = "invalid-key-format"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(malformedKey, userId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for path traversal in key`() {
        // Given
        val userId = 123L
        val traversalKey = "../../../etc/passwd"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(traversalKey, userId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for extremely long key`() {
        // Given
        val userId = 123L
        val longKey = "images/" + "a".repeat(1000) + ".jpg"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(longKey, userId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }

    @Test
    fun `confirmUpload - failure - throws IllegalArgumentException for key with special characters`() {
        // Given
        val userId = 123L
        val specialKey = "images/test@image#$.jpg"

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            (imageUploadRequester as ImageUploadTracker).confirmUpload(specialKey, userId)
        }
        assertTrue(exception.message!!.contains("이미지 업로드 실패"))
    }
}
