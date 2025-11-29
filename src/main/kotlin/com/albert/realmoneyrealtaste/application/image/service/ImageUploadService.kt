package com.albert.realmoneyrealtaste.application.image.service

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPostResponse
import com.albert.realmoneyrealtaste.application.image.exception.ImageConfirmUploadException
import com.albert.realmoneyrealtaste.application.image.exception.ImageGenerateException
import com.albert.realmoneyrealtaste.application.image.provided.ImageKeyGenerator
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadRequester
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadTracker
import com.albert.realmoneyrealtaste.application.image.provided.ImageUploadValidator
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.application.image.required.PresignedUrlGenerator
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.command.ImageCreateCommand
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ImageUploadService(
    private val presignedUrlGenerator: PresignedUrlGenerator,
    private val imageRepository: ImageRepository,
    private val imageUploadValidator: ImageUploadValidator,
    private val imageReader: ImageReader,
    private val imageKeyGenerator: ImageKeyGenerator,
) : ImageUploadRequester, ImageUploadTracker {

    private val logger = LoggerFactory.getLogger(ImageUploadService::class.java)

    override fun generatePresignedPostUrl(request: ImageUploadRequest, userId: Long): PresignedPostResponse {
        try {
            // 1. 사용자 검증
            val todayUploadCount = imageReader.getTodayUploadCount(userId)
            imageUploadValidator.validateUserUploadLimit(todayUploadCount)

            // 2. 이미지 메타데이터 검증
            imageUploadValidator.validateImageRequest(request)

            // 3. 안전한 파일 키 생성
            val imageKey = imageKeyGenerator.generateSecureImageKey(request.fileName)

            // 4. Presigned PUT URL 생성
            val presignedPut = presignedUrlGenerator.generatePresignedPutUrl(imageKey, request)

            logger.info("Generated presigned POST URL for user: $userId, key: $imageKey")

            return presignedPut
        } catch (e: IllegalArgumentException) {
            throw ImageGenerateException("이미지 업로드 실패", e)
        }
    }

    override fun confirmUpload(key: String, userId: Long): ImageUploadResult {
        try {
            val imageCreateCommand = ImageCreateCommand(
                fileKey = FileKey(key),
                uploadedBy = userId,
                imageType = ImageType.POST_IMAGE,
            )
            val image = imageRepository.save(Image.create(imageCreateCommand))

            return ImageUploadResult(
                success = true,
                imageId = image.requireId()
            )
        } catch (e: IllegalArgumentException) {
            throw ImageConfirmUploadException("이미지 업로드 실패", e)
        }
    }
}
