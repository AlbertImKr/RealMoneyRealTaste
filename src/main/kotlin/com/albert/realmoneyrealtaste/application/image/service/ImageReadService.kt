package com.albert.realmoneyrealtaste.application.image.service

import com.albert.realmoneyrealtaste.application.image.dto.ImageInfo
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.application.image.exception.ImageNotFoundException
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import com.albert.realmoneyrealtaste.application.image.required.CloudStorage
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.value.FileKey
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ImageReadService(
    private val imageRepository: ImageRepository,
    private val cloudStorage: CloudStorage,
) : ImageReader {

    override fun getImageUrl(imageId: Long, userId: Long): String {
        val image = imageRepository.findByIdAndIsDeletedFalse(imageId)
            ?: throw ImageNotFoundException("이미지를 찾을 수 없습니다: $imageId")

        require(image.canAccess(userId)) { "이미지에 접근 권한이 없습니다" }

        return cloudStorage.generatePresignedUrl(image.fileKey.value)
    }

    override fun getImagesByMember(userId: Long): List<ImageInfo> {
        val images = imageRepository.findByUploadedByAndIsDeletedFalse(userId)
        return images.map { image ->
            ImageInfo(
                imageId = image.requireId(),
                fileKey = image.fileKey.value,
                imageType = image.imageType,
                url = cloudStorage.generatePresignedUrl(image.fileKey.value)
            )
        }
    }

    override fun getTodayUploadCount(userId: Long): Int {
        return imageRepository.countByUploadedByAndIsDeletedFalse(userId)
    }

    override fun getUploadStatus(key: FileKey): ImageUploadResult {
        val image = imageRepository.findByFileKeyAndIsDeletedFalse(key)
        return image?.let { ImageUploadResult(true, it.requireId()) }
            ?: ImageUploadResult(false, -1)
    }

    override fun getImage(imageId: Long, userId: Long): Image {
        return imageRepository.findByIdAndIsDeletedFalse(imageId)
            ?: throw IllegalArgumentException("이미지를 찾을 수 없습니다: $imageId")
    }
}
