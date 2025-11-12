package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 활성화 토큰을 찾을 수 없을 때 발생하는 예외
 *
 * @param message 예외 메시지
 */
class ActivationTokenNotFoundException(message: String) : MemberApplicationException(message)
