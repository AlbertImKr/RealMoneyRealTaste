package com.albert.realmoneyrealtaste.application.post.exception

sealed class PostApplicationException(message: String, cause: Throwable) :
    IllegalArgumentException(message, cause)
