package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 회원을 찾을 수 없을 때 발생하는 예외
 *
 * @param message 예외 메시지 (기본값: "해당 회원을 찾을 수 없습니다.")
 */
class MemberNotFoundException(message: String = "해당 회원을 찾을 수 없습니다.") : ApplicationException(message)
