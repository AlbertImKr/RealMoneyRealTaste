package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 유효하지 않은 활성화 토큰에 대해 발생하는 예외
 *
 * @param message 예외 메시지 (기본값: "유효하지 않은 토큰입니다.")
 */
class InvalidActivationTokenException(message: String = "유효하지 않은 토큰입니다.") : MemberDomainException(message)
