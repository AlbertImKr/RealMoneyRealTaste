package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest
import com.albert.realmoneyrealtaste.application.image.dto.PresignedPostResponse

fun interface ImageUploadRequester {
    fun generatePresignedPostUrl(request: ImageUploadRequest, userId: Long): PresignedPostResponse
}
