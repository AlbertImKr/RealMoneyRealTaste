package com.albert.realmoneyrealtaste.application.member.exception

class AlreadyActivatedException(message: String = "이미 활성화된 계정입니다.") : RuntimeException(message)
