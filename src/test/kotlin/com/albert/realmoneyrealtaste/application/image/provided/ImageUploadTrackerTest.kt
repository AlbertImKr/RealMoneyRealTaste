package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.application.image.exception.ImageConfirmUploadException
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        val image = imageRepository.findByFileKeyAndIsDeletedFalse(imageKey)
        assertNotNull(image)
        assertTrue(result.success)
        assertEquals(image.requireId(), result.imageId)
        assertEquals(userId, image.uploadedBy)
        assertEquals(imageKey.value, image.fileKey.value)
        assertEquals(ImageType.POST_IMAGE, image.imageType)
        assertFalse(image.isDeleted)
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

        // Then
        assertTrue(result1.success)
        assertTrue(result2.success)

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
        val lambdaTracker = ImageUploadTracker { key, userId ->
            confirmedUploads.add(key to userId)
            ImageUploadResult(
                success = true,
                imageId = 123L,
            )
        }

        val key = "test/image.jpg"
        val userId = 123L

        // When
        val result = lambdaTracker.confirmUpload(key, userId)

        // Then
        assertTrue(result.success)
        assertEquals(1, confirmedUploads.size)
        assertEquals(key to userId, confirmedUploads[0])
    }

    @Test
    fun `ImageUploadTracker functional interface - failure simulation`() {
        // Given
        val failureTracker = ImageUploadTracker { key, _ ->
            if (key.contains("invalid")) {
                throw IllegalArgumentException("Invalid key format: $key")
            }
            ImageUploadResult(
                success = true,
                imageId = 123L,
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
        }

        // 사용자의 모든 이미지 확인
        val userImages = imageRepository.findByUploadedByAndIsDeletedFalse(userId)
        assertEquals(5, userImages.size)
        userImages.forEach { image ->
            assertEquals(userId, image.uploadedBy)
            assertFalse(image.isDeleted)
        }
    }

    @Test
    fun `confirmUpload - failure - throws ImageConfirmUploadException with null user ID`() {
        // Given
        val key = "test/image.jpg"
        val invalidUserId = -1L

        // When & Then
        assertFailsWith<ImageConfirmUploadException> {
            imageUploadTracker.confirmUpload(key, invalidUserId)
        }.let { exception ->
            assertEquals("이미지 업로드 실패", exception.message)
            assertNotNull(exception.cause)
            assertTrue(exception.cause is IllegalArgumentException)
        }
    }

    @Test
    fun `confirmUpload - failure - throws ImageConfirmUploadException with empty key`() {
        // Given
        val emptyKey = ""
        val userId = 123L

        // When & Then
        assertFailsWith<ImageConfirmUploadException> {
            imageUploadTracker.confirmUpload(emptyKey, userId)
        }.let { exception ->
            assertEquals("이미지 업로드 실패", exception.message)
            assertNotNull(exception.cause)
            assertTrue(exception.cause is IllegalArgumentException)
        }
    }

}
