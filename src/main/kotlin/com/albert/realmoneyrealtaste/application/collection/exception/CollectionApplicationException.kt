package com.albert.realmoneyrealtaste.application.collection.exception

sealed class CollectionApplicationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
