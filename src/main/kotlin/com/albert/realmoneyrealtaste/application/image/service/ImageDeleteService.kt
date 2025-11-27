package com.albert.realmoneyrealtaste.application.image.service

import com.albert.realmoneyrealtaste.application.image.exception.ImageDeleteException
import com.albert.realmoneyrealtaste.application.image.provided.ImageDeleter
import com.albert.realmoneyrealtaste.application.image.provided.ImageReader
import com.albert.realmoneyrealtaste.application.image.required.ImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ImageDeleteService(
    private val imageRepository: ImageRepository,
    private val imageReader: ImageReader,
) : ImageDeleter {

    override fun deleteImage(imageId: Long, userId: Long) {
        try {
            val image = imageReader.getImage(imageId, userId)

            require(image.canAccess(userId)) { "이미지를 삭제할 권한이 없습니다" }

            image.markAsDeleted()
            imageRepository.save(image)
        } catch (e: IllegalArgumentException) {
            throw ImageDeleteException("이미지 삭제 실패: ${e.message}", e)
        }
    }
}
