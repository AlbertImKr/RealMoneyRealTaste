package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.domain.image.Image
import org.springframework.data.repository.Repository

interface ImageRepository : Repository<Image, Long> {
    fun save(image: Image): Image
    fun findById(id: Long): Image?
    fun findByUploadedByAndIsDeletedFalse(uploadedBy: Long): List<Image>
    fun findByIdAndIsDeletedFalse(id: Long): Image?
    fun findByFileKeyAndIsDeletedFalse(fileKey: com.albert.realmoneyrealtaste.domain.image.value.FileKey): Image?
    fun countByUploadedByAndIsDeletedFalse(uploadedBy: Long): Int
    fun findAllByIdInAndIsDeletedFalse(ids: List<Long>): List<Image>
}
