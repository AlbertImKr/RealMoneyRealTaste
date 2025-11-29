package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageInfo
import com.albert.realmoneyrealtaste.application.image.exception.ImageNotFoundException
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageReaderTest(
    val imageReader: ImageReader,
    val imageRepository: ImageRepository,
) : IntegrationTestBase() {

    @Test
    fun `getImageUrl - success - returns presigned URL for own image`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When
        val imageUrl = imageReader.getImageUrl(savedImage.requireId(), userId)

        // Then
        assertNotNull(imageUrl)
        assertTrue(imageUrl.isNotEmpty())
        assertTrue(imageUrl.contains(savedImage.fileKey.value))
    }

    @Test
    fun `getImageUrl - failure - throws exception when image does not exist`() {
        // Given
        val userId = 123L
        val nonExistentImageId = 999999L

        // When & Then
        assertFailsWith<ImageNotFoundException> {
            imageReader.getImageUrl(nonExistentImageId, userId)
        }
    }

    @Test
    fun `getImageUrl - failure - throws exception when unauthorized user tries to access`() {
        // Given
        val ownerId = 123L
        val unauthorizedUserId = 456L
        val image = createTestImage(ownerId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageReader.getImageUrl(savedImage.requireId(), unauthorizedUserId)
        }
    }

    @Test
    fun `getImagesByMember - success - returns list of user's images`() {
        // Given
        val userId = 123L
        val imageTypes = listOf(ImageType.POST_IMAGE, ImageType.PROFILE_IMAGE, ImageType.THUMBNAIL)
        val savedImages = imageTypes.map { imageType ->
            val image = createTestImage(userId, imageType)
            imageRepository.save(image)
        }

        // When
        val imageInfos = imageReader.getImagesByMember(userId)

        // Then
        assertEquals(3, imageInfos.size)
        imageInfos.forEach { imageInfo ->
            assertEquals(userId, getUploadedByFromImageInfo(imageInfo))
            assertNotNull(imageInfo.url)
            assertTrue(imageInfo.url.isNotEmpty())
        }

        // ID 순서로 정렬 확인
        val sortedImageInfos = imageInfos.sortedBy { it.imageId }
        val sortedSavedImages = savedImages.sortedBy { it.requireId() }

        sortedImageInfos.zip(sortedSavedImages) { info, image ->
            assertEquals(image.requireId(), info.imageId)
            assertEquals(image.fileKey.value, info.fileKey)
            assertEquals(image.imageType, info.imageType)
        }
    }

    @Test
    fun `getImagesByMember - success - returns empty list for user with no images`() {
        // Given
        val userId = 999999L

        // When
        val imageInfos = imageReader.getImagesByMember(userId)

        // Then
        assertEquals(0, imageInfos.size)
    }

    @Test
    fun `getImagesByMember - success - excludes deleted images`() {
        // Given
        val userId = 123L
        val activeImage = createTestImage(userId, ImageType.POST_IMAGE)
        val deletedImage = createTestImage(userId, ImageType.PROFILE_IMAGE)

        val savedActiveImage = imageRepository.save(activeImage)
        val savedDeletedImage = imageRepository.save(deletedImage)

        // 삭제 처리
        savedDeletedImage.markAsDeleted()
        imageRepository.save(savedDeletedImage)

        // When
        val imageInfos = imageReader.getImagesByMember(userId)

        // Then
        assertEquals(1, imageInfos.size)
        assertEquals(savedActiveImage.requireId(), imageInfos[0].imageId)
    }

    @Test
    fun `getTodayUploadCount - success - returns correct count for user with images`() {
        // Given
        val userId = 123L
        val images = (1..5).map {
            createTestImage(userId, ImageType.POST_IMAGE)
        }
        images.forEach { imageRepository.save(it) }

        // When
        val uploadCount = imageReader.getTodayUploadCount(userId)

        // Then
        assertEquals(5, uploadCount)
    }

    @Test
    fun `getTodayUploadCount - success - returns zero for user with no images`() {
        // Given
        val userId = 999999L

        // When
        val uploadCount = imageReader.getTodayUploadCount(userId)

        // Then
        assertEquals(0, uploadCount)
    }

    @Test
    fun `getTodayUploadCount - success - excludes deleted images from count`() {
        // Given
        val userId = 123L
        val activeImages = (1..3).map {
            createTestImage(userId, ImageType.POST_IMAGE)
        }
        val deletedImages = (1..2).map {
            createTestImage(userId, ImageType.PROFILE_IMAGE)
        }

        activeImages.map { imageRepository.save(it) }
        val savedDeletedImages = deletedImages.map { imageRepository.save(it) }

        // 삭제 처리
        savedDeletedImages.forEach { deletedImage ->
            deletedImage.markAsDeleted()
            imageRepository.save(deletedImage)
        }

        // When
        val uploadCount = imageReader.getTodayUploadCount(userId)

        // Then
        assertEquals(3, uploadCount)
    }

    @Test
    fun `getUploadStatus - success - returns success true for existing image`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When
        val uploadResult = imageReader.getUploadStatus(savedImage.fileKey)

        // Then
        assertTrue(uploadResult.success)
        assertEquals(savedImage.requireId(), uploadResult.imageId)
    }

    @Test
    fun `getUploadStatus - success - returns success false for non-existing image`() {
        // Given
        val nonExistentKey = FileKey("non-existent/image.jpg")

        // When
        val uploadResult = imageReader.getUploadStatus(nonExistentKey)

        // Then
        assertFalse(uploadResult.success)
        assertEquals(-1, uploadResult.imageId)
    }

    @Test
    fun `getUploadStatus - success - excludes deleted images`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // 삭제 처리
        savedImage.markAsDeleted()
        imageRepository.save(savedImage)

        // When
        val uploadResult = imageReader.getUploadStatus(savedImage.fileKey)

        // Then
        assertFalse(uploadResult.success)
        assertEquals(-1, uploadResult.imageId)
    }

    @Test
    fun `getImage - success - returns image for valid ID`() {
        // Given
        val userId = 123L
        val image = createTestImage(userId, ImageType.POST_IMAGE)
        val savedImage = imageRepository.save(image)

        // When
        val retrievedImage = imageReader.getImage(savedImage.requireId(), userId)

        // Then
        assertEquals(savedImage.requireId(), retrievedImage.requireId())
        assertEquals(savedImage.fileKey.value, retrievedImage.fileKey.value)
        assertEquals(savedImage.uploadedBy, retrievedImage.uploadedBy)
        assertEquals(savedImage.imageType, retrievedImage.imageType)
        assertFalse(retrievedImage.isDeleted)
    }

    @Test
    fun `getImage - failure - throws exception for non-existing image`() {
        // Given
        val userId = 123L
        val nonExistentImageId = 999999L

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageReader.getImage(nonExistentImageId, userId)
        }
    }

    @Test
    fun `getImage - success - boundary values`() {
        // Given - 최소값
        val minUserId = 1L
        val minImage = createTestImage(minUserId, ImageType.POST_IMAGE)
        val savedMinImage = imageRepository.save(minImage)

        // When
        val retrievedMinImage = imageReader.getImage(savedMinImage.requireId(), minUserId)

        // Then
        assertEquals(savedMinImage.requireId(), retrievedMinImage.requireId())

        // Given - 최대값
        val maxUserId = Long.MAX_VALUE
        val maxImage = createTestImage(maxUserId, ImageType.PROFILE_IMAGE)
        val savedMaxImage = imageRepository.save(maxImage)

        // When
        val retrievedMaxImage = imageReader.getImage(savedMaxImage.requireId(), maxUserId)

        // Then
        assertEquals(savedMaxImage.requireId(), retrievedMaxImage.requireId())
    }

    @Test
    fun `getImagesByMember - success - multiple users images are isolated`() {
        // Given
        val user1Id = 123L
        val user2Id = 456L

        val user1Images = (1..3).map {
            createTestImage(user1Id, ImageType.POST_IMAGE)
        }
        val user2Images = (1..2).map {
            createTestImage(user2Id, ImageType.PROFILE_IMAGE)
        }

        user1Images.forEach { imageRepository.save(it) }
        user2Images.forEach { imageRepository.save(it) }

        // When
        val user1ImageInfos = imageReader.getImagesByMember(user1Id)
        val user2ImageInfos = imageReader.getImagesByMember(user2Id)

        // Then
        assertEquals(3, user1ImageInfos.size)
        assertEquals(2, user2ImageInfos.size)

        // 사용자 격리 확인
        user1ImageInfos.forEach { info ->
            assertEquals(user1Id, getUploadedByFromImageInfo(info))
        }
        user2ImageInfos.forEach { info ->
            assertEquals(user2Id, getUploadedByFromImageInfo(info))
        }
    }

    private fun createTestImage(userId: Long, imageType: ImageType): Image {
        val command = ImageCreateCommand(
            fileKey = FileKey("${UUID.randomUUID()}/test-image-${System.currentTimeMillis()}.jpg"),
            uploadedBy = userId,
            imageType = imageType
        )
        return Image.create(command)
    }

    // ImageInfo에서 uploadedBy를 추출하는 헬퍼 메서드
    // (실제로는 ImageInfo에 uploadedBy가 없으므로 이미지 ID로 조회)
    private fun getUploadedByFromImageInfo(imageInfo: ImageInfo): Long {
        val image = imageRepository.findById(imageInfo.imageId)
        return image?.uploadedBy ?: throw IllegalStateException("Image not found: ${imageInfo.imageId}")
    }
}
