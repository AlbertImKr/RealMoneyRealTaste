package com.albert.realmoneyrealtaste.application.member.exception

class SendPasswordResetEmailException(
    message: String,
    cause: Throwable,
) : MemberApplicationException(message, cause)
