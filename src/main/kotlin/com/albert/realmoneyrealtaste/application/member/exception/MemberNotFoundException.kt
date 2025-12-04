package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 멤버 비활성화 중에 발생하는 예외
 *
 * @param message 예외 메시지
 */
class MemberNotFoundException(message: String) : MemberApplicationException(message)
