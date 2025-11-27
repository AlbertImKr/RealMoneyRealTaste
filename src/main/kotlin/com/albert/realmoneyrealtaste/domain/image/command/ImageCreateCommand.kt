package com.albert.realmoneyrealtaste.domain.image.command

import com.albert.realmoneyrealtaste.domain.image.ImageType
import com.albert.realmoneyrealtaste.domain.image.value.FileKey

data class ImageCreateCommand(
    val fileKey: FileKey,
    val uploadedBy: Long,
    val imageType: ImageType,
) {

    companion object {
        const val MIN_VALID_VALUE = 0

        const val ERROR_INVALID_UPLOADER_ID = "업로더 ID는 0보다 커야 합니다"
    }

    init {
        // 기본 검증
        require(uploadedBy > MIN_VALID_VALUE) { ERROR_INVALID_UPLOADER_ID }
    }
}
