package com.albert.realmoneyrealtaste.application.image.provided

fun interface ImageKeyGenerator {
    fun generateSecureImageKey(fileName: String): String
}
