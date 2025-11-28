package com.albert.realmoneyrealtaste.application.image.required

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPostResponse

interface PresignedUrlGenerator {
    fun generatePresignedPutUrl(imageKey: String, request: ImageUploadRequest): PresignedPostResponse

    fun generatePresignedGetUrl(imageKey: String): String
}
