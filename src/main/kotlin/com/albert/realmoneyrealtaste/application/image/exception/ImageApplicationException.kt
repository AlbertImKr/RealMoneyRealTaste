package com.albert.realmoneyrealtaste.application.image.exception

sealed class ImageApplicationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
