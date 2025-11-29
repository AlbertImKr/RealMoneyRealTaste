package com.albert.realmoneyrealtaste.application.image.dto

import java.time.Instant

data class PresignedPutResponse(
    val uploadUrl: String,
    val key: String,
    val expiresAt: Instant,
    val metadata: Map<String, String>,
)
