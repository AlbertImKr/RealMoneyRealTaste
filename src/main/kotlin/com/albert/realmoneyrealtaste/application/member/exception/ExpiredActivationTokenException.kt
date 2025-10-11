package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 만료된 활성화 토큰에 대해 발생하는 예외
 *
 * @param message 예외 메시지 (기본값: "만료된 토큰입니다.")
 */
class ExpiredActivationTokenException(message: String = "만료된 토큰입니다.") : RuntimeException(message)
