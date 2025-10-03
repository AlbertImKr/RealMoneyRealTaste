package com.albert.realmoneyrealtaste.application.member.exception

class InvalidActivationTokenException(message: String = "유효하지 않은 토큰입니다.") : RuntimeException(message)
