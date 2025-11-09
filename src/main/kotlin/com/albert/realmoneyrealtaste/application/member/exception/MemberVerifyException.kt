package com.albert.realmoneyrealtaste.application.member.exception

class MemberVerifyException(
    message: String,
    cause: Throwable,
) : MemberApplicationException(message, cause)
