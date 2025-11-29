package com.albert.realmoneyrealtaste.domain.image.command

import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("ImageCreateCommand 테스트")
class ImageCreateCommandTest {

    @Test
    @DisplayName("ImageCreateCommand 생성 - 성공")
    fun `constructor - success - creates valid command`() {
        // Given
        val fileKey = FileKey("test-image.jpg")
        val uploadedBy = 1L
        val imageType = ImageType.POST_IMAGE

        // When
        val command = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = uploadedBy,
            imageType = imageType,
        )

        // Then
        assertEquals(fileKey, command.fileKey)
        assertEquals(uploadedBy, command.uploadedBy)
        assertEquals(imageType, command.imageType)
    }

    @Test
    @DisplayName("ImageCreateCommand 생성 - 모든 이미지 타입")
    fun `constructor - success - creates command with all image types`() {
        // Given
        val fileKey = FileKey("test.jpg")
        val uploadedBy = 123L

        // When & Then
        val postCommand = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = uploadedBy,
            imageType = ImageType.POST_IMAGE,
        )
        assertEquals(ImageType.POST_IMAGE, postCommand.imageType)

        val profileCommand = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = uploadedBy,
            imageType = ImageType.PROFILE_IMAGE,
        )
        assertEquals(ImageType.PROFILE_IMAGE, profileCommand.imageType)

        val thumbnailCommand = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = uploadedBy,
            imageType = ImageType.THUMBNAIL,
        )
        assertEquals(ImageType.THUMBNAIL, thumbnailCommand.imageType)
    }

    @Test
    @DisplayName("ImageCreateCommand 생성 - 다양한 파일키")
    fun `constructor - success - creates command with various file keys`() {
        // Given
        val uploadedBy = 1L
        val imageType = ImageType.POST_IMAGE
        val fileKeys = listOf(
            "simple.jpg",
            "with-dash.png",
            "with_underscore.webp",
            "complex-name-123.jpg",
            "path/to/file.jpg",
            "images/2024/01/01/uuid.jpg",
            "users/123/profile/avatar.jpg",
            "thumbnails/posts/456/thumb.jpg"
        )

        // When & Then
        fileKeys.forEach { key ->
            val command = ImageCreateCommand(
                fileKey = FileKey(key),
                uploadedBy = uploadedBy,
                imageType = imageType,
            )
            assertEquals(key, command.fileKey.value)
            assertEquals(uploadedBy, command.uploadedBy)
            assertEquals(imageType, command.imageType)
        }
    }

    @Test
    @DisplayName("ImageCreateCommand 생성 - 유효한 업로더 ID")
    fun `constructor - success - creates command with valid uploader IDs`() {
        // Given
        val fileKey = FileKey("test.jpg")
        val imageType = ImageType.POST_IMAGE
        val validIds = listOf(1L, 100L, 999999L, Long.MAX_VALUE)

        // When & Then
        validIds.forEach { uploadedBy ->
            val command = ImageCreateCommand(
                fileKey = fileKey,
                uploadedBy = uploadedBy,
                imageType = imageType,
            )
            assertEquals(uploadedBy, command.uploadedBy)
        }
    }

    @Test
    @DisplayName("ImageCreateCommand 생성 - 실패 - 업로더 ID가 0")
    fun `constructor - failure - uploader ID is zero`() {
        // Given
        val fileKey = FileKey("test.jpg")
        val uploadedBy = 0L
        val imageType = ImageType.POST_IMAGE

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            ImageCreateCommand(
                fileKey = fileKey,
                uploadedBy = uploadedBy,
                imageType = imageType,
            )
        }
        assertEquals("업로더 ID는 0보다 커야 합니다", exception.message)
    }

    @Test
    @DisplayName("ImageCreateCommand 생성 - 실패 - 음수 업로더 ID")
    fun `constructor - failure - uploader ID is negative`() {
        // Given
        val fileKey = FileKey("test.jpg")
        val negativeIds = listOf(-1L, -100L, -999999L, Long.MIN_VALUE)
        val imageType = ImageType.POST_IMAGE

        // When & Then
        negativeIds.forEach { uploadedBy ->
            val exception = assertThrows<IllegalArgumentException> {
                ImageCreateCommand(
                    fileKey = fileKey,
                    uploadedBy = uploadedBy,
                    imageType = imageType,
                )
            }
            assertEquals("업로더 ID는 0보다 커야 합니다", exception.message)
        }
    }

    @Test
    @DisplayName("ImageCreateCommand 상수 확인")
    fun `constants - success - validates constant values`() {
        // Given & When & Then
        assertEquals(0, ImageCreateCommand.MIN_VALID_VALUE)
        assertEquals("업로더 ID는 0보다 커야 합니다", ImageCreateCommand.ERROR_INVALID_UPLOADER_ID)
    }

    @Test
    @DisplayName("ImageCreateCommand data class 기능")
    fun `data class features - success - tests equals, hashCode, toString`() {
        // Given
        val command1 = ImageCreateCommand(
            fileKey = FileKey("test.jpg"),
            uploadedBy = 123L,
            imageType = ImageType.POST_IMAGE,
        )
        val command2 = ImageCreateCommand(
            fileKey = FileKey("test.jpg"),
            uploadedBy = 123L,
            imageType = ImageType.POST_IMAGE,
        )
        val command3 = ImageCreateCommand(
            fileKey = FileKey("different.jpg"),
            uploadedBy = 123L,
            imageType = ImageType.POST_IMAGE,
        )

        // When & Then - equals
        assertEquals(command1, command2)
        assertEquals(command1.hashCode(), command2.hashCode())
        assertEquals(command1.toString(), command2.toString())

        // Different values
        assert(command1 != command3)
        assert(command1.hashCode() != command3.hashCode())

        // copy function
        val copiedCommand = command1.copy(uploadedBy = 456L)
        assertEquals(456L, copiedCommand.uploadedBy)
        assertEquals(command1.fileKey, copiedCommand.fileKey)
        assertEquals(command1.imageType, copiedCommand.imageType)
    }

    @Test
    @DisplayName("ImageCreateCommand 실제 시나리오")
    fun `real world scenarios - success - tests practical usage patterns`() {
        // Scenario 1: 사용자 프로필 이미지 업로드
        val profileCommand = ImageCreateCommand(
            fileKey = FileKey("users/123/profile/avatar.jpg"),
            uploadedBy = 123L,
            imageType = ImageType.PROFILE_IMAGE,
        )
        assertEquals("users/123/profile/avatar.jpg", profileCommand.fileKey.value)
        assertEquals(123L, profileCommand.uploadedBy)
        assertEquals(ImageType.PROFILE_IMAGE, profileCommand.imageType)

        // Scenario 2: 게시글 첨부 이미지
        val postCommand = ImageCreateCommand(
            fileKey = FileKey("posts/456/content/image1.jpg"),
            uploadedBy = 456L,
            imageType = ImageType.POST_IMAGE,
        )
        assertEquals("posts/456/content/image1.jpg", postCommand.fileKey.value)
        assertEquals(456L, postCommand.uploadedBy)
        assertEquals(ImageType.POST_IMAGE, postCommand.imageType)

        // Scenario 3: 썸네일 생성
        val thumbnailCommand = ImageCreateCommand(
            fileKey = FileKey("thumbnails/posts/456/thumb.jpg"),
            uploadedBy = 456L,
            imageType = ImageType.THUMBNAIL,
        )
        assertEquals("thumbnails/posts/456/thumb.jpg", thumbnailCommand.fileKey.value)
        assertEquals(456L, thumbnailCommand.uploadedBy)
        assertEquals(ImageType.THUMBNAIL, thumbnailCommand.imageType)
    }

    @Test
    @DisplayName("ImageCreateCommand 경계값 테스트")
    fun `boundary values - success - tests edge cases`() {
        // Given
        val fileKey = FileKey("test.jpg")
        val imageType = ImageType.POST_IMAGE

        // When & Then - 최소 유효값
        val minValidCommand = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = 1L, // MIN_VALID_VALUE + 1
            imageType = imageType,
        )
        assertEquals(1L, minValidCommand.uploadedBy)

        // When & Then - 최대값
        val maxValidCommand = ImageCreateCommand(
            fileKey = fileKey,
            uploadedBy = Long.MAX_VALUE,
            imageType = imageType,
        )
        assertEquals(Long.MAX_VALUE, maxValidCommand.uploadedBy)
    }

    @Test
    @DisplayName("ImageCreateCommand 구조 분해 할당")
    fun `destructuring - success - tests component functions`() {
        // Given
        val command = ImageCreateCommand(
            fileKey = FileKey("test.jpg"),
            uploadedBy = 123L,
            imageType = ImageType.POST_IMAGE,
        )

        // When - 구조 분해 할당
        val (fileKey, uploadedBy, imageType) = command

        // Then
        assertEquals(FileKey("test.jpg"), fileKey)
        assertEquals(123L, uploadedBy)
        assertEquals(ImageType.POST_IMAGE, imageType)
    }
}
