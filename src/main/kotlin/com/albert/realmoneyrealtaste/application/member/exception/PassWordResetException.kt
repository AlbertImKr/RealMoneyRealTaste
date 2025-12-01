package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 비밀번호 초기화 중에 발생하는 예외
 *
 * @param message 예외 메시지
 */
class PassWordResetException(message: String) : MemberApplicationException(message)
