package com.albert.realmoneyrealtaste.application.image.required

interface CloudStorage {
    fun generatePresignedUrl(fileKey: String): String
    fun delete(fileKey: String)
}
