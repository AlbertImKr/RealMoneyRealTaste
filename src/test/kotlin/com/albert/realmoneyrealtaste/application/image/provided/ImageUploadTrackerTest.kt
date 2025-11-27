package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageUploadTrackerTest(
    val imageUploadTracker: ImageUploadTracker,
    val imageRepository: ImageRepository,
) : IntegrationTestBase() {

    @Test
    fun `confirmUpload - success - confirms upload and creates image record`() {
        // Given
        val userId = 123L
        val imageKey = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")

        // When
        val result = imageUploadTracker.confirmUpload(imageKey.value, userId)
        flushAndClear()

        // Then
        assertTrue(result.success)
        assertEquals(imageKey.value, result.key)
        assertNotNull(result.url)
        assertTrue(result.url.isNotEmpty())

        // 데이터베이스에 이미지 레코드가 생성되었는지 확인
        val savedImage = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey)
        assertNotNull(savedImage)
        assertEquals(userId, savedImage.uploadedBy)
        assertEquals(imageKey.value, savedImage.fileKey.value)
        assertEquals(ImageType.POST_IMAGE, savedImage.imageType)
        assertFalse(savedImage.isDeleted)
    }

    @Test
    fun `confirmUpload - success - handles multiple confirmations for different users`() {
        // Given
        val user1Id = 123L
        val user2Id = 456L
        val imageKey1 = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")
        val imageKey2 = FileKey("images/2024/01/01/${UUID.randomUUID()}.png")

        // When
        val result1 = imageUploadTracker.confirmUpload(imageKey1.value, user1Id)
        val result2 = imageUploadTracker.confirmUpload(imageKey2.value, user2Id)
        flushAndClear()

        // Then
        assertTrue(result1.success)
        assertTrue(result2.success)
        assertEquals(imageKey1.value, result1.key)
        assertEquals(imageKey2.value, result2.key)

        // 각 사용자의 이미지가 올바르게 생성되었는지 확인
        val savedImage1 = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey1)
        val savedImage2 = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey2)

        assertNotNull(savedImage1)
        assertNotNull(savedImage2)
        assertEquals(user1Id, savedImage1.uploadedBy)
        assertEquals(user2Id, savedImage2.uploadedBy)
    }

    @Test
    fun `confirmUpload - success - handles various file extensions`() {
        // Given
        val userId = 123L
        val testCases = listOf(
            "images/2024/01/01/${UUID.randomUUID()}.jpg",
            "images/2024/01/01/${UUID.randomUUID()}.png",
            "images/2024/01/01/${UUID.randomUUID()}.webp",
            "images/2024/01/01/${UUID.randomUUID()}.jpeg"
        )

        // When & Then
        testCases.forEach { key ->
            val result = imageUploadTracker.confirmUpload(key, userId)

            assertTrue(result.success)
            assertEquals(key, result.key)
            assertNotNull(result.url)

            // 데이터베이스 확인
            val fileKey = FileKey(key)
            val savedImage = imageRepository.findByFileKeyAndIsDeletedFalse(fileKey)
            assertNotNull(savedImage)
            assertEquals(userId, savedImage.uploadedBy)
            assertEquals(fileKey.value, savedImage.fileKey.value)
        }
    }

    @Test
    fun `confirmUpload - success - boundary user IDs`() {
        // Given - 최소 사용자 ID
        val minUserId = 1L
        val imageKey = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")

        // When
        val minResult = imageUploadTracker.confirmUpload(imageKey.value, minUserId)
        flushAndClear()

        // Then
        assertTrue(minResult.success)
        assertEquals(imageKey.value, minResult.key)

        val savedMinImage = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey)
        assertNotNull(savedMinImage)
        assertEquals(minUserId, savedMinImage.uploadedBy)

        // Given - 최대 사용자 ID
        val maxUserId = Long.MAX_VALUE
        val maxImageKey = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")

        // When
        val maxResult = imageUploadTracker.confirmUpload(maxImageKey.value, maxUserId)
        flushAndClear()

        // Then
        assertTrue(maxResult.success)
        assertEquals(maxImageKey.value, maxResult.key)

        val savedMaxImage = imageRepository.findByFileKeyAndIsDeletedFalse(maxImageKey)
        assertNotNull(savedMaxImage)
        assertEquals(maxUserId, savedMaxImage.uploadedBy)
    }

    @Test
    fun `confirmUpload - success - generates proper URL format`() {
        // Given
        val userId = 123L
        val imageKey = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")

        // When
        val result = imageUploadTracker.confirmUpload(imageKey.value, userId)

        // Then
        assertTrue(result.success)
        assertNotNull(result.url)
        assertTrue(result.url.contains(imageKey.value))
        assertTrue(result.url.startsWith("https://") || result.url.startsWith("http://"))
    }

    @Test
    fun `confirmUpload - success - handles complex key structures`() {
        // Given
        val userId = 123L
        val complexKeys = listOf(
            "images/2024/01/01/${UUID.randomUUID()}/test-image.jpg",
            "images/2024/12/31/${UUID.randomUUID()}/nested/path/image.png",
            "images/2024/06/15/${UUID.randomUUID()}/very/deep/nested/directory/structure/webp-image.webp"
        )

        // When & Then
        complexKeys.forEach { key ->
            val result = imageUploadTracker.confirmUpload(key, userId)

            assertTrue(result.success)
            assertEquals(key, result.key)
            assertNotNull(result.url)

            // 데이터베이스 확인
            val fileKey = FileKey(key)
            val savedImage = imageRepository.findByFileKeyAndIsDeletedFalse(fileKey)
            assertNotNull(savedImage)
            assertEquals(userId, savedImage.uploadedBy)
            assertEquals(key, savedImage.fileKey.value)
        }
    }

    @Test
    fun `confirmUpload - success - maintains data consistency`() {
        // Given
        val userId = 123L
        val imageKey = FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")

        // When
        val result = imageUploadTracker.confirmUpload(imageKey.value, userId)
        flushAndClear()

        // Then
        assertTrue(result.success)

        // 여러 번 조회해도 동일한 결과
        val savedImage1 = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey)
        val savedImage2 = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey)

        assertNotNull(savedImage1)
        assertNotNull(savedImage2)
        assertEquals(savedImage1.requireId(), savedImage2.requireId())
        assertEquals(savedImage1.fileKey.value, savedImage2.fileKey.value)
        assertEquals(savedImage1.uploadedBy, savedImage2.uploadedBy)
    }

    @Test
    fun `ImageUploadTracker functional interface - lambda implementation`() {
        // Given
        val confirmedUploads = mutableListOf<Pair<String, Long>>()
        val lambdaTracker: ImageUploadTracker = ImageUploadTracker { key, userId ->
            confirmedUploads.add(key to userId)
            ImageUploadResult(
                success = true,
                key = key,
                url = "https://lambda-generated.example.com/$key"
            )
        }

        val key = "test/image.jpg"
        val userId = 123L

        // When
        val result = lambdaTracker.confirmUpload(key, userId)

        // Then
        assertTrue(result.success)
        assertEquals(key, result.key)
        assertEquals("https://lambda-generated.example.com/test/image.jpg", result.url)
        assertEquals(1, confirmedUploads.size)
        assertEquals(key to userId, confirmedUploads[0])
    }

    @Test
    fun `ImageUploadTracker functional interface - failure simulation`() {
        // Given
        val failureTracker: ImageUploadTracker = ImageUploadTracker { key, userId ->
            if (key.contains("invalid")) {
                throw IllegalArgumentException("Invalid key format: $key")
            }
            ImageUploadResult(
                success = true,
                key = key,
                url = "https://example.com/$key"
            )
        }

        // When & Then - 성공 케이스
        val successResult = failureTracker.confirmUpload("valid/image.jpg", 123L)
        assertTrue(successResult.success)

        // When & Then - 실패 케이스
        try {
            failureTracker.confirmUpload("invalid/image.jpg", 123L)
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid key format: invalid/image.jpg", e.message)
        }
    }

    @Test
    fun `confirmUpload - success - multiple uploads same user`() {
        // Given
        val userId = 123L
        val imageKeys = (1..5).map {
            FileKey("images/2024/01/01/${UUID.randomUUID()}.jpg")
        }

        // When
        val results = imageKeys.map { imageKey ->
            imageUploadTracker.confirmUpload(imageKey.value, userId)
        }
        flushAndClear()

        // Then
        assertEquals(5, results.size)
        results.forEach { result ->
            assertTrue(result.success)
            assertNotNull(result.url)
        }

        // 사용자의 모든 이미지 확인
        val userImages = imageRepository.findByUploadedByAndIsDeletedFalse(userId)
        assertEquals(5, userImages.size)
        userImages.forEach { image ->
            assertEquals(userId, image.uploadedBy)
            assertFalse(image.isDeleted)
        }
    }

    // 테스트용 보조 클래스
    private class TestImageUploadTracker {
        private val confirmedUploads = mutableSetOf<Pair<String, Long>>()

        fun confirmUpload(key: String, userId: Long): ImageUploadResult {
            confirmedUploads.add(key to userId)
            return ImageUploadResult(
                success = true,
                key = key,
                url = "method-ref-test/$key"
            )
        }

        fun isConfirmed(key: String, userId: Long): Boolean {
            return confirmedUploads.contains(key to userId)
        }
    }
}
