package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.application.image.dto.ImageInfo
import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult
import com.albert.realmoneyrealtaste.domain.image.Image
import com.albert.realmoneyrealtaste.domain.image.value.FileKey

interface ImageReader {
    fun getImageUrl(imageId: Long, userId: Long): String
    fun getImagesByMember(userId: Long): List<ImageInfo>
    fun getTodayUploadCount(userId: Long): Int
    fun getUploadStatus(key: FileKey): ImageUploadResult
    fun getImage(imageId: Long, userId: Long): Image
    fun readImagesByIds(imageIds: List<Long>): List<ImageInfo>
}
