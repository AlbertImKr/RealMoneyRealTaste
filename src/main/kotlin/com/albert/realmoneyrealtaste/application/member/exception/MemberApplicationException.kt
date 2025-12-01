package com.albert.realmoneyrealtaste.application.member.exception

/**
 * 멤버 애플리케이션 관련 예외의 최상위 클래스
 *
 * @param message 예외 메시지
 */
sealed class MemberApplicationException(message: String) : IllegalArgumentException(message)
