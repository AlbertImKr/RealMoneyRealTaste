package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 비밀번호 초기화 토큰이 없을 때 발생하는 예외
 *
 * @param message 예외 메시지
 */
class PasswordResetTokenNotFoundException(message: String) : MemberApplicationException(message)
