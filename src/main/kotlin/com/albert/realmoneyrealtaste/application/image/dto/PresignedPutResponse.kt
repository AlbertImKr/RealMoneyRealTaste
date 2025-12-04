package com.albert.realmoneyrealtaste.application.image.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class PresignedPutResponse(
    val uploadUrl: String,
    val key: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC+9")
    val expiresAt: Instant,
    val metadata: Map<String, String>,
)
