package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 만료된 비밀번호 재설정 토큰 예외
 *
 * @param message 예외 메시지 (기본값: "만료된 비밀번호 재설정 토큰입니다.")
 */
class ExpiredPasswordResetTokenException(message: String = "만료된 비밀번호 재설정 토큰입니다.") : ApplicationException(message)
