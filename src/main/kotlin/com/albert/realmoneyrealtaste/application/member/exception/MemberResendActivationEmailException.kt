package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 멤버 활성화 이메일 재전송 중에 발생하는 예외
 *
 * @param message 예외 메시지
 */
class MemberResendActivationEmailException(message: String) : MemberApplicationException(message)
