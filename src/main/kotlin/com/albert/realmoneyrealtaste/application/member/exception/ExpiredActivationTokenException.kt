package com.albert.realmoneyrealtaste.application.member.exception

class ExpiredActivationTokenException(message: String = "만료된 토큰입니다.") : RuntimeException(message)
