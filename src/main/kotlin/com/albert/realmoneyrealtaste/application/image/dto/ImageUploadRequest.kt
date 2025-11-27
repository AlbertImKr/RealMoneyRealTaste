package com.albert.realmoneyrealtaste.application.image.dto

import com.albert.realmoneyrealtaste.domain.image.ImageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 이미지 업로드 요청 DTO
 */
data class ImageUploadRequest(
    @field:NotBlank(message = "파일 이름은 필수입니다.")
    val fileName: String,

    @field:Positive(message = "파일 크기는 0보다 커야 합니다.")
    val fileSize: Long,

    @field:NotBlank(message = "파일 타입은 필수입니다.")
    val contentType: String,

    @field:Positive(message = "이미지 너비는 0보다 커야 합니다.")
    val width: Int,

    @field:Positive(message = "이미지 높이는 0보다 커야 합니다.")
    val height: Int,

    @field:NotNull(message = "이미지 타입은 필수입니다.")
    val imageType: ImageType,
)
