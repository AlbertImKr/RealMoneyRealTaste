package com.albert.realmoneyrealtaste.application.member.exception

class MemberNotFoundException(message: String = "해당 회원을 찾을 수 없습니다.") : RuntimeException(message)
