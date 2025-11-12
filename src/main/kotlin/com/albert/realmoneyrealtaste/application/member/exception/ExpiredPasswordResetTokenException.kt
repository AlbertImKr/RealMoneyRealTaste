package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 만료된 비밀번호 재설정 토큰에 대해 발생하는 예외
 *
 * @param message 예외 메시지
 */
class ExpiredPasswordResetTokenException(message: String) : MemberApplicationException(message)
