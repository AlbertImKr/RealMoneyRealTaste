package com.albert.realmoneyrealtaste.domain.image

import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("Image 도메인 테스트")
class ImageTest {

    @Test
    @DisplayName("Image 생성 - 성공")
    fun `create - success - creates image with valid command`() {
        // Given
        val command = ImageCreateCommand(
            fileKey = FileKey("test-image.jpg"),
            uploadedBy = 1L,
            imageType = ImageType.POST_IMAGE,
        )

        // When
        val image = Image.create(command)

        // Then
        assertEquals("test-image.jpg", image.fileKey.value)
        assertEquals(1L, image.uploadedBy)
        assertEquals(ImageType.POST_IMAGE, image.imageType)
        assertFalse(image.isDeleted)
    }

    @Test
    @DisplayName("Image 생성 - 모든 이미지 타입")
    fun `create - success - creates all image types`() {
        // Given & When & Then
        val postImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("post.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        assertEquals(ImageType.POST_IMAGE, postImage.imageType)
        assertTrue(postImage.isPostImage())
        assertFalse(postImage.isProfileImage())
        assertFalse(postImage.isThumbnail())

        val profileImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("profile.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        assertEquals(ImageType.PROFILE_IMAGE, profileImage.imageType)
        assertFalse(profileImage.isPostImage())
        assertTrue(profileImage.isProfileImage())
        assertFalse(profileImage.isThumbnail())

        val thumbnailImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("thumb.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.THUMBNAIL,
            )
        )
        assertEquals(ImageType.THUMBNAIL, thumbnailImage.imageType)
        assertFalse(thumbnailImage.isPostImage())
        assertFalse(thumbnailImage.isProfileImage())
        assertTrue(thumbnailImage.isThumbnail())
    }

    @Test
    @DisplayName("canAccess - 성공 - 소유자 접근")
    fun `canAccess - success - owner can access`() {
        // Given
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test.jpg"),
                uploadedBy = 123L,
                imageType = ImageType.POST_IMAGE,
            )
        )

        // When & Then
        assertTrue(image.canAccess(123L))
    }

    @Test
    @DisplayName("canAccess - 실패 - 다른 사용자 접근")
    fun `canAccess - failure - other user cannot access`() {
        // Given
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test.jpg"),
                uploadedBy = 123L,
                imageType = ImageType.POST_IMAGE,
            )
        )

        // When & Then
        assertFalse(image.canAccess(456L))
    }

    @Test
    @DisplayName("markAsDeleted - 성공 - 이미지 삭제 표시")
    fun `markAsDeleted - success - marks image as deleted`() {
        // Given
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        assertFalse(image.isDeleted)

        // When
        image.markAsDeleted()

        // Then
        assertTrue(image.isDeleted)
    }

    @Test
    @DisplayName("isProfileImage - 성공 - 프로필 이미지 확인")
    fun `isProfileImage - success - identifies profile image`() {
        // Given
        val profileImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("profile.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        val postImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("post.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        val thumbnailImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("thumb.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.THUMBNAIL,
            )
        )

        // When & Then
        assertTrue(profileImage.isProfileImage())
        assertFalse(postImage.isProfileImage())
        assertFalse(thumbnailImage.isProfileImage())
    }

    @Test
    @DisplayName("isPostImage - 성공 - 게시글 이미지 확인")
    fun `isPostImage - success - identifies post image`() {
        // Given
        val profileImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("profile.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        val postImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("post.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        val thumbnailImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("thumb.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.THUMBNAIL,
            )
        )

        // When & Then
        assertFalse(profileImage.isPostImage())
        assertTrue(postImage.isPostImage())
        assertFalse(thumbnailImage.isPostImage())
    }

    @Test
    @DisplayName("isThumbnail - 성공 - 썸네일 이미지 확인")
    fun `isThumbnail - success - identifies thumbnail image`() {
        // Given
        val profileImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("profile.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        val postImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("post.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        val thumbnailImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("thumb.jpg"),
                uploadedBy = 1L,
                imageType = ImageType.THUMBNAIL,
            )
        )

        // When & Then
        assertFalse(profileImage.isThumbnail())
        assertFalse(postImage.isThumbnail())
        assertTrue(thumbnailImage.isThumbnail())
    }

    @Test
    @DisplayName("Image 생성 - 다양한 파일키")
    fun `create - success - creates image with various file keys`() {
        // Given
        val fileKeys = listOf(
            "simple.jpg",
            "with-dash.png",
            "with_underscore.webp",
            "complex-name-123.jpg",
            "path/to/file.jpg",
            "images/2024/01/01/uuid.jpg"
        )

        // When & Then
        fileKeys.forEach { key ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey(key),
                    uploadedBy = 1L,
                    imageType = ImageType.POST_IMAGE,
                )
            )
            assertEquals(key, image.fileKey.value)
        }
    }

    @Test
    @DisplayName("Image 생성 - 다양한 사용자 ID")
    fun `create - success - creates image with various user IDs`() {
        // Given
        val userIds = listOf(1L, 100L, 999999L)

        // When & Then
        userIds.forEach { userId ->
            val image = Image.create(
                ImageCreateCommand(
                    fileKey = FileKey("test.jpg"),
                    uploadedBy = userId,
                    imageType = ImageType.POST_IMAGE,
                )
            )
            assertEquals(userId, image.uploadedBy)
        }
    }

    @Test
    @DisplayName("Image 상태 변경 - 삭제 후 접근 권한 확인")
    fun `image state - deleted image still maintains access rights`() {
        // Given
        val image = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("test.jpg"),
                uploadedBy = 123L,
                imageType = ImageType.POST_IMAGE,
            )
        )

        // When
        image.markAsDeleted()

        // Then
        assertTrue(image.isDeleted)
        assertTrue(image.canAccess(123L)) // 삭제된 이미지도 소유자는 접근 가능
        assertFalse(image.canAccess(456L))
        assertEquals(ImageType.POST_IMAGE, image.imageType) // 이미지 타입은 변경되지 않음
    }

    @Test
    @DisplayName("Image 생성 시나리오 - 실제 사용 패턴")
    fun `create - real world scenarios`() {
        // Scenario 1: 사용자 프로필 이미지 업로드
        val profileImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("users/123/profile/avatar.jpg"),
                uploadedBy = 123L,
                imageType = ImageType.PROFILE_IMAGE,
            )
        )
        assertTrue(profileImage.isProfileImage())
        assertTrue(profileImage.canAccess(123L))

        // Scenario 2: 게시글 첨부 이미지
        val postImage = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("posts/456/content/image1.jpg"),
                uploadedBy = 456L,
                imageType = ImageType.POST_IMAGE,
            )
        )
        assertTrue(postImage.isPostImage())
        assertTrue(postImage.canAccess(456L))

        // Scenario 3: 썸네일 생성
        val thumbnail = Image.create(
            ImageCreateCommand(
                fileKey = FileKey("thumbnails/posts/456/thumb.jpg"),
                uploadedBy = 456L,
                imageType = ImageType.THUMBNAIL,
            )
        )
        assertTrue(thumbnail.isThumbnail())
        assertTrue(thumbnail.canAccess(456L))
    }

    @Test
    fun `setter - success - for covering framework`() {
        val image = TestImage()

        val newFileKey = FileKey("updated-file.jpg")
        image.setFileKeyForTest(newFileKey)
        assertEquals("updated-file.jpg", image.fileKey.value)

        image.setUploadedByForTest(999L)
        assertEquals(999L, image.uploadedBy)

        image.setImageTypeForTest(ImageType.PROFILE_IMAGE)
        assertEquals(ImageType.PROFILE_IMAGE, image.imageType)

        image.setIsDeletedForTest(true)
        assertTrue(image.isDeleted)
    }

    private class TestImage : Image(
        fileKey = FileKey("test-file.jpg"),
        uploadedBy = 123L,
        imageType = ImageType.POST_IMAGE,
        isDeleted = false,
    ) {
        fun setFileKeyForTest(fileKey: FileKey) {
            this.fileKey = fileKey
        }

        fun setUploadedByForTest(uploadedBy: Long) {
            this.uploadedBy = uploadedBy
        }

        fun setImageTypeForTest(imageType: ImageType) {
            this.imageType = imageType
        }

        fun setIsDeletedForTest(isDeleted: Boolean) {
            this.isDeleted = isDeleted
        }
    }
}
