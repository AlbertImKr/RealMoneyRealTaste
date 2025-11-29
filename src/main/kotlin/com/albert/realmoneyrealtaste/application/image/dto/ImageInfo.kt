package com.albert.realmoneyrealtaste.application.image.dto

import com.albert.realmoneyrealtaste.domain.image.ImageType

data class ImageInfo(
    val imageId: Long,
    val fileKey: String,
    val imageType: ImageType,
    val url: String,
)
