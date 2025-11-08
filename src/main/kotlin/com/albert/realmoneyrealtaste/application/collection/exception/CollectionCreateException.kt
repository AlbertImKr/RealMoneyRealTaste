package com.albert.realmoneyrealtaste.application.collection.exception

class CollectionCreateException(
    message: String,
    cause: Throwable? = null,
) : CollectionApplicationException(message, cause)
