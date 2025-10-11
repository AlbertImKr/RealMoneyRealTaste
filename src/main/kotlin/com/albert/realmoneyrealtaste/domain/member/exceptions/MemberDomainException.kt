package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 회원 도메인 관련 예외의 최상위 클래스
 */
sealed class MemberDomainException(message: String) : RuntimeException(message)
