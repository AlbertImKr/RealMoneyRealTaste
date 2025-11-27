package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.domain.image.ImageType
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ImageUploadValidatorTest(
    val imageUploadValidator: ImageUploadValidator,
) : IntegrationTestBase() {

    @Test
    fun `validateUserUploadLimit - success - allows upload within limit`() {
        // Given
        val todayUploadCount = 25 // 기본값 50 미만

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateUserUploadLimit(todayUploadCount)
    }

    @Test
    fun `validateUserUploadLimit - success - allows upload at limit boundary`() {
        // Given
        val todayUploadCount = 49 // 기본값 50 - 1

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateUserUploadLimit(todayUploadCount)
    }

    @Test
    fun `validateUserUploadLimit - failure - throws exception when limit exceeded`() {
        // Given
        val todayUploadCount = 50 // 기본값 50 이상

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageUploadValidator.validateUserUploadLimit(todayUploadCount)
        }
    }

    @Test
    fun `validateUserUploadLimit - failure - throws exception when limit greatly exceeded`() {
        // Given
        val todayUploadCount = 100 // 기본값 50 초과

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageUploadValidator.validateUserUploadLimit(todayUploadCount)
        }
    }

    @Test
    fun `validateImageRequest - success - validates valid post image request`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "valid-image.jpg",
            fileSize = 1024L,
            contentType = "image/jpeg",
            width = 800,
            height = 600,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(request)
    }

    @Test
    fun `validateImageRequest - success - validates valid profile image request`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "profile.png",
            fileSize = 2048L,
            contentType = "image/png",
            width = 300,
            height = 300,
            imageType = ImageType.PROFILE_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(request)
    }

    @Test
    fun `validateImageRequest - success - validates valid thumbnail image request`() {
        // Given
        val request = ImageUploadRequest(
            fileName = "thumb.webp",
            fileSize = 512L,
            contentType = "image/webp",
            width = 50,
            height = 50,
            imageType = ImageType.THUMBNAIL
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(request)
    }

    @Test
    fun `validateImageRequest - success - validates boundary values for post image`() {
        // Given - 최소값
        val minRequest = ImageUploadRequest(
            fileName = "min.jpg",
            fileSize = 1L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(minRequest)

        // Given - 최대값
        val maxRequest = ImageUploadRequest(
            fileName = "a".repeat(251) + ".jpg", // 255자 제한
            fileSize = 5242880L, // 5MB
            contentType = "image/jpeg",
            width = 2000,
            height = 2000,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(maxRequest)
    }

    @Test
    fun `validateImageRequest - success - validates boundary values for profile image`() {
        // Given - 최소값
        val minProfileRequest = ImageUploadRequest(
            fileName = "profile-min.jpg",
            fileSize = 1L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.PROFILE_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(minProfileRequest)

        // Given - 최대값
        val maxProfileRequest = ImageUploadRequest(
            fileName = "profile-max.jpg",
            fileSize = 5242880L,
            contentType = "image/jpeg",
            width = 500,
            height = 500,
            imageType = ImageType.PROFILE_IMAGE
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(maxProfileRequest)
    }

    @Test
    fun `validateImageRequest - success - validates boundary values for thumbnail image`() {
        // Given - 최소값
        val minThumbRequest = ImageUploadRequest(
            fileName = "thumb-min.jpg",
            fileSize = 1L,
            contentType = "image/jpeg",
            width = 16,
            height = 16,
            imageType = ImageType.THUMBNAIL
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(minThumbRequest)

        // Given - 최대값
        val maxThumbRequest = ImageUploadRequest(
            fileName = "thumb-max.jpg",
            fileSize = 5242880L,
            contentType = "image/jpeg",
            width = 100,
            height = 100,
            imageType = ImageType.THUMBNAIL
        )

        // When & Then - 예외 발생하지 않음
        imageUploadValidator.validateImageRequest(maxThumbRequest)
    }

    @Test
    fun `validateImageRequest - success - validates various allowed file names`() {
        // Given
        val validFileNames = listOf(
            "simple.jpg",
            "file_name.png",
            "file-name.webp",
            "file.jpeg",
            "a.jpg", // 최소 길이
            "very-long-filename-with-many-dashes-and-underscores.jpg",
            "123.jpg", // 숫자로 시작
            "file123.jpg", // 숫자 포함
            "ALL_CAPS.JPG", // 대문자
            "mixed_Case-Name.jpg" // 대소문자 혼합
        )

        // When & Then
        validFileNames.forEach { fileName ->
            val request = ImageUploadRequest(
                fileName = fileName,
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 200,
                height = 200,
                imageType = ImageType.POST_IMAGE
            )
            imageUploadValidator.validateImageRequest(request) // 예외 발생하지 않음
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid file size`() {
        // Given - 파일 크기 0
        val zeroSizeRequest = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 0L,
            contentType = "image/jpeg",
            width = 200,
            height = 200,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageUploadValidator.validateImageRequest(zeroSizeRequest)
        }

        // Given - 파일 크기 초과
        val oversizedRequest = ImageUploadRequest(
            fileName = "test.jpg",
            fileSize = 5242881L, // 5MB + 1
            contentType = "image/jpeg",
            width = 200,
            height = 200,
            imageType = ImageType.POST_IMAGE
        )

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            imageUploadValidator.validateImageRequest(oversizedRequest)
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid content type`() {
        // Given
        val invalidContentTypeRequests = listOf(
            "text/plain",
            "application/pdf",
            "image/gif",
            "image/bmp",
            "video/mp4",
            "audio/mp3",
            ""
        )

        // When & Then
        invalidContentTypeRequests.forEach { contentType ->
            val request = ImageUploadRequest(
                fileName = "test.jpg",
                fileSize = 1024L,
                contentType = contentType,
                width = 200,
                height = 200,
                imageType = ImageType.POST_IMAGE
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid dimensions - post image`() {
        // Given
        val invalidDimensionRequests = listOf(
            99 to 200, // 너비 부족
            200 to 99, // 높이 부족
            2001 to 200, // 너비 초과
            200 to 2001, // 높이 초과
            99 to 99, // 둘 다 부족
            2001 to 2001 // 둘 다 초과
        )

        // When & Then
        invalidDimensionRequests.forEach { (width, height) ->
            val request = ImageUploadRequest(
                fileName = "test.jpg",
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = width,
                height = height,
                imageType = ImageType.POST_IMAGE
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid dimensions - profile image`() {
        // Given
        val invalidProfileRequests = listOf(
            99 to 200, // 너비 부족
            200 to 99, // 높이 부족
            501 to 200, // 너비 초과
            200 to 501, // 높이 초과
        )

        // When & Then
        invalidProfileRequests.forEach { (width, height) ->
            val request = ImageUploadRequest(
                fileName = "profile.jpg",
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = width,
                height = height,
                imageType = ImageType.PROFILE_IMAGE
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid dimensions - thumbnail image`() {
        // Given
        val invalidThumbRequests = listOf(
            15 to 50, // 너비 부족
            50 to 15, // 높이 부족
            101 to 50, // 너비 초과
            50 to 101, // 높이 초과
        )

        // When & Then
        invalidThumbRequests.forEach { (width, height) ->
            val request = ImageUploadRequest(
                fileName = "thumb.jpg",
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = width,
                height = height,
                imageType = ImageType.THUMBNAIL
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid file name`() {
        // Given
        val invalidFileNameRequests = listOf(
            "", // 빈 파일명
            "   ", // 공백만
            "no-extension", // 확장자 없음
            "file.", // 확장자만 점
            ".hidden", // 점으로 시작
            "file@name.jpg", // 특수문자 @
            "file#name.jpg", // 특수문자 #
            "file name.jpg", // 공백
            "file(name).jpg", // 괄호
            "file+name.jpg", // 플러스
            "file=name.jpg", // 등호
            "file&name.jpg", // 앰퍼샌드
            "file%name.jpg", // 퍼센트
            "file?name.jpg", // 물음표
            "file|name.jpg", // 파이프
            "file<name>.jpg", // 꺽쇠
            "file>name>.jpg", // 꺽쇠
            "file*name.jpg", // 별표
            "file\"name.jpg", // 따옴표
            "file'name.jpg", // 작은따옴표
            "file`name.jpg", // 백틱
            "file~name.jpg", // 틸드
            "file!name.jpg", // 느낌표
            "file^name.jpg", // 캐럿
            "file{name}.jpg", // 중괄호
            "file}name.jpg", // 중괄호
            "file[name].jpg", // 대괄호
            "file]name.jpg", // 대괄호
            "file;name.jpg", // 세미콜론
            "file:name.jpg", // 콜론
            "file,name.jpg", // 쉼표
            "file\\name.jpg", // 백슬래시
            "file/name.jpg", // 슬래시 (허용되지 않음)
            "file..name.jpg", // 연속 점
            "file.txt", // 허용되지 않는 확장자
            "file.exe", // 실행 파일
            "file.php", // 스크립트 파일
            "a".repeat(256) + ".jpg" // 파일명 너무 김
        )

        // When & Then
        invalidFileNameRequests.forEach { fileName ->
            val request = ImageUploadRequest(
                fileName = fileName,
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 200,
                height = 200,
                imageType = ImageType.POST_IMAGE
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - failure - throws exception for invalid file extensions`() {
        // Given
        val invalidExtensionRequests = listOf(
            "file.txt",
            "file.pdf",
            "file.doc",
            "file.exe",
            "file.php",
            "file.jsp",
            "file.asp",
            "file.sh",
            "file.bat",
            "file.gif",
            "file.bmp",
            "file.tiff",
            "file.svg"
        )

        // When & Then
        invalidExtensionRequests.forEach { fileName ->
            val request = ImageUploadRequest(
                fileName = fileName,
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 200,
                height = 200,
                imageType = ImageType.POST_IMAGE
            )
            assertFailsWith<IllegalArgumentException> {
                imageUploadValidator.validateImageRequest(request)
            }
        }
    }

    @Test
    fun `validateImageRequest - success - validates case insensitive extensions`() {
        // Given
        val caseInsensitiveRequests = listOf(
            "file.JPG",
            "file.JPEG",
            "file.PNG",
            "file.WEBP",
            "file.Jpg",
            "file.Jpeg",
            "file.Png",
            "file.Webp"
        )

        // When & Then
        caseInsensitiveRequests.forEach { fileName ->
            val request = ImageUploadRequest(
                fileName = fileName,
                fileSize = 1024L,
                contentType = "image/jpeg",
                width = 200,
                height = 200,
                imageType = ImageType.POST_IMAGE
            )
            imageUploadValidator.validateImageRequest(request) // 예외 발생하지 않음
        }
    }
}
