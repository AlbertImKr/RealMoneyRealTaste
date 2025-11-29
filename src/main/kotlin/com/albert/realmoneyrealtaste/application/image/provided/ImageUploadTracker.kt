package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadResult

fun interface ImageUploadTracker {
    fun confirmUpload(key: String, userId: Long): ImageUploadResult
}
