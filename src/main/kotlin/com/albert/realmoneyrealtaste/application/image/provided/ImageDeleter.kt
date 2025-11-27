package com.albert.realmoneyrealtaste.application.image.provided

fun interface ImageDeleter {
    fun deleteImage(imageId: Long, userId: Long)
}
