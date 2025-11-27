package com.albert.realmoneyrealtaste.application.image.dto

import java.time.Instant

data class PresignedPostResponse(
    val uploadUrl: String,
    val key: String,
    val fields: Map<String, String>,
    val expiresAt: Instant,
)
