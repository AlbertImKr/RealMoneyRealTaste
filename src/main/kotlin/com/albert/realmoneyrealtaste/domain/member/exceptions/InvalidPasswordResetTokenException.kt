package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 유효하지 않은 비밀번호 재설정 토큰 예외
 *
 * @param message 예외 메시지 (기본값: "유효하지 않은 비밀번호 재설정 토큰입니다.")
 */
class InvalidPasswordResetTokenException(message: String = "유효하지 않은 비밀번호 재설정 토큰입니다.") : MemberDomainException(message)
