package com.albert.realmoneyrealtaste.application.image.service

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadValidator
import com.albert.realmoneyrealtaste.domain.image.ImageType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ImageUploadValidateService(
    @Value("\${image.upload.max-size-bytes:5242880}") private val maxSizeBytes: Long,
    @Value("\${image.upload.daily-limit-per-user:50}") private val dailyLimit: Int,
) : ImageUploadValidator {

    companion object {
        private val ALLOWED_CONTENT_TYPES = listOf("image/jpeg", "image/png", "image/webp")
        private val ALLOWED_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp")
        private const val MAX_FILE_NAME_LENGTH = 255

        // 썸네일: 작은 크기, 아이콘용
        private const val MAX_THUMBNAIL_DIMENSION = 100
        private const val MIN_THUMBNAIL_DIMENSION = 16

        // 프로필 이미지: 정사각형, 중간 크기
        private const val MAX_PROFILE_DIMENSION = 500
        private const val MIN_PROFILE_DIMENSION = 100

        // 게시글 이미지: 큰 크기, 다양한 비율 허용
        private const val MAX_POST_DIMENSION = 2000
        private const val MIN_POST_DIMENSION = 100
    }

    override fun validateUserUploadLimit(todayUploadCount: Int) {
        require(todayUploadCount < dailyLimit) {
            "일일 업로드 제한 초과: $todayUploadCount/$dailyLimit"
        }
    }

    override fun validateImageRequest(request: ImageUploadRequest) {
        validateFileSize(request.fileSize)
        validateContentType(request.contentType)
        validateDimensions(request.width, request.height, request.imageType)
        validateFileName(request.fileName)
    }

    private fun validateFileSize(fileSize: Long) {
        require(fileSize in 1..maxSizeBytes) {
            "파일 크기는 1 byte ~ ${maxSizeBytes / 1024 / 1024}MB 사이여야 합니다"
        }
    }

    private fun validateContentType(contentType: String) {
        require(contentType in ALLOWED_CONTENT_TYPES) {
            "지원하지 않는 파일 형식: $contentType"
        }
    }

    private fun validateDimensions(width: Int, height: Int, imageType: ImageType) {
        when (imageType) {
            ImageType.POST_IMAGE -> {
                require(width in MIN_POST_DIMENSION..MAX_POST_DIMENSION && height in MIN_POST_DIMENSION..MAX_POST_DIMENSION) {
                    "게시글 이미지 크기는 ${MIN_POST_DIMENSION}x${MIN_POST_DIMENSION} ~ ${MAX_POST_DIMENSION}x${MAX_POST_DIMENSION}px 사이여야 합니다"
                }
            }

            ImageType.PROFILE_IMAGE -> {
                require(width in MIN_PROFILE_DIMENSION..MAX_PROFILE_DIMENSION && height in MIN_PROFILE_DIMENSION..MAX_PROFILE_DIMENSION) {
                    "프로필 이미지 크기는 ${MIN_PROFILE_DIMENSION}x${MIN_PROFILE_DIMENSION} ~ ${MAX_PROFILE_DIMENSION}x${MAX_PROFILE_DIMENSION}px 사이여야 합니다"
                }
            }

            ImageType.THUMBNAIL -> {
                require(width in MIN_THUMBNAIL_DIMENSION..MAX_THUMBNAIL_DIMENSION && height in MIN_THUMBNAIL_DIMENSION..MAX_THUMBNAIL_DIMENSION) {
                    "썸네일 이미지 크기는 ${MIN_THUMBNAIL_DIMENSION}x${MIN_THUMBNAIL_DIMENSION} ~ ${MAX_THUMBNAIL_DIMENSION}x${MAX_THUMBNAIL_DIMENSION}px 사이여야 합니다"
                }
            }
        }
    }

    private fun validateFileName(fileName: String) {
        require(fileName.isNotBlank()) {
            "파일명은 비어있을 수 없습니다"
        }
        require(fileName.length <= MAX_FILE_NAME_LENGTH) {
            "파일명은 $MAX_FILE_NAME_LENGTH 자 이하여야 합니다"
        }
        require(!fileName.contains("..")) {
            "파일명에 잘못된 문자열이 포함되어 있습니다"
        }

        // 확장자 검증
        val extension = fileName.substringAfterLast(".", "").lowercase()
        require(extension.isNotEmpty()) {
            "파일 확장자가 필요합니다"
        }
        require(extension in ALLOWED_EXTENSIONS) {
            "허용되지 않는 파일 확장자: $extension"
        }
    }
}
