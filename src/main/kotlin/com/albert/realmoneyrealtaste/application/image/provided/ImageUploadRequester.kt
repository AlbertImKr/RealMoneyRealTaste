package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPutResponse

fun interface ImageUploadRequester {
    fun generatePresignedUploadUrl(request: ImageUploadRequest, userId: Long): PresignedPutResponse
}
