package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse

interface PresignedUrlGenerator {
    fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPutResponse

    fun generatePresignedGetUrl(imageKey: String): String
}
