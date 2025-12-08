package com.albert.realmoneyrealtaste.application.event.exception

sealed class MemberEventApplicationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
