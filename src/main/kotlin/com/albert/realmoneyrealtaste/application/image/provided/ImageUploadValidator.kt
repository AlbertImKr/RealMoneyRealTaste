package com.albert.realmoneyrealtaste.application.image.provided

import com.albert.realmoneyrealtaste.application.image.dto.ImageUploadRequest

interface ImageUploadValidator {

    fun validateUserUploadLimit(todayUploadCount: Int)

    fun validateImageRequest(request: ImageUploadRequest)
}
