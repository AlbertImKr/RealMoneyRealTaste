package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.exception.ImageDeleteException
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageDeleterTest(
    val imageDeleter: ImageDeleter,
    val imageRepository: ImageRepository,
) : IntegrationTestBase() {

    @Test
    fun `deleteImage - success - deletes own image`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When
        imageDeleter.deleteImage(savedImage.requireId(), userId)
        flushAndClear()

        // Then
        val deletedImage = imageRepository.findById(savedImage.requireId())
        assertTrue(deletedImage!!.isDeleted)
    }

    @Test
    fun `deleteImage - success - deletes various image types`() {
        // Given
        val userId = 123L
        val imageTypes = listOf(ImageType.POST_IMAGE, ImageType.PROFILE_IMAGE, ImageType.THUMBNAIL)
        val savedImages = imageTypes.map { imageType ->
            val image = createTestImage(userId, imageType)
            imageRepository.save(image)
        }

        // When
        savedImages.forEach { savedImage ->
            imageDeleter.deleteImage(savedImage.requireId(), userId)
        }
        flushAndClear()

        // Then
        savedImages.forEach { savedImage ->
            val deletedImage = imageRepository.findById(savedImage.requireId())
            assertTrue(deletedImage!!.isDeleted)
        }
    }

    @Test
    fun `deleteImage - failure - throws exception when unauthorized user tries to delete`() {
        // Given
        val ownerId = 123L
        val unauthorizedUserId = 456L
        val image = createTestImage(ownerId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When & Then
        assertFailsWith<ImageDeleteException> {
            imageDeleter.deleteImage(savedImage.requireId(), unauthorizedUserId)
        }

        // Then - 이미지가 삭제되지 않았는지 확인
        val notDeletedImage = imageRepository.findById(savedImage.requireId())
        assertFalse(notDeletedImage!!.isDeleted)
    }

    @Test
    fun `deleteImage - failure - throws exception when image does not exist`() {
        // Given
        val userId = 123L
        val nonExistentImageId = 999999L

        // When & Then
        assertFailsWith<ImageDeleteException> {
            imageDeleter.deleteImage(nonExistentImageId, userId)
        }
    }

    @Test
    fun `deleteImage - failure - throws exception when trying to delete already deleted image`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // 이미 삭제 처리
        savedImage.markAsDeleted()
        imageRepository.save(savedImage)
        flushAndClear()

        // When & Then
        assertFailsWith<ImageDeleteException> {
            imageDeleter.deleteImage(savedImage.requireId(), userId)
        }
    }

    @Test
    fun `deleteImage - success - boundary values`() {
        // Given - 최소값
        val minUserId = 1L
        val minImage = createTestImage(minUserId, ImageType.POST_IMAGE)
        val savedMinImage = imageRepository.save(minImage)

        // When
        imageDeleter.deleteImage(savedMinImage.requireId(), minUserId)
        flushAndClear()

        // Then
        val deletedMinImage = imageRepository.findById(savedMinImage.requireId())
        assertTrue(deletedMinImage!!.isDeleted)

        // Given - 최대값
        val maxUserId = Long.MAX_VALUE
        val maxImage = createTestImage(maxUserId, ImageType.PROFILE_IMAGE)
        val savedMaxImage = imageRepository.save(maxImage)

        // When
        imageDeleter.deleteImage(savedMaxImage.requireId(), maxUserId)
        flushAndClear()

        // Then
        val deletedMaxImage = imageRepository.findById(savedMaxImage.requireId())
        assertTrue(deletedMaxImage!!.isDeleted)
    }

    @Test
    fun `deleteImage - success - multiple deletions by same user`() {
        // Given
        val userId = 123L
        val images = (1..5).map {
            createTestImage(userId, ImageType.POST_IMAGE)
        }
        val savedImages = images.map { imageRepository.save(it) }

        // When
        savedImages.forEach { savedImage ->
            imageDeleter.deleteImage(savedImage.requireId(), userId)
        }
        flushAndClear()

        // Then
        savedImages.forEach { savedImage ->
            val deletedImage = imageRepository.findById(savedImage.requireId())
            assertTrue(deletedImage!!.isDeleted)
        }
    }

    @Test
    fun `deleteImage - failure - user cannot delete other user's images`() {
        // Given
        val user1Id = 123L
        val user2Id = 456L

        val user1Image = createTestImage(user1Id, ImageType.POST_IMAGE)
        val user2Image = createTestImage(user2Id, ImageType.POST_IMAGE)

        val savedUser1Image = imageRepository.save(user1Image)
        val savedUser2Image = imageRepository.save(user2Image)

        // When & Then - User1이 User2의 이미지 삭제 시도
        assertFailsWith<ImageDeleteException> {
            imageDeleter.deleteImage(savedUser2Image.requireId(), user1Id)
        }

        // When & Then - User2가 User1의 이미지 삭제 시도
        assertFailsWith<ImageDeleteException> {
            imageDeleter.deleteImage(savedUser1Image.requireId(), user2Id)
        }

        // Then - 두 이미지 모두 삭제되지 않았는지 확인
        val notDeletedUser1Image = imageRepository.findById(savedUser1Image.requireId())
        val notDeletedUser2Image = imageRepository.findById(savedUser2Image.requireId())
        assertFalse(notDeletedUser1Image!!.isDeleted)
        assertFalse(notDeletedUser2Image!!.isDeleted)
    }

    private fun createTestImage(userId: Long, imageType: ImageType): Image {
        val command = ImageCreateCommand(
            fileKey = FileKey(UUID.randomUUID().toString() + "/test-image-${System.currentTimeMillis()}.jpg"),
            uploadedBy = userId,
            imageType = imageType
        )
        return Image.create(command)
    }
}
