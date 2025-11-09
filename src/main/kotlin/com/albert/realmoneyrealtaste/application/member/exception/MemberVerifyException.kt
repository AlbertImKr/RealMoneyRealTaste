package com.albert.realmoneyrealtaste.application.member.exception

class MemberVerifyException(
    message: String,
    cause: Throwable? = null,
) : MemberApplicationException(message, cause)
