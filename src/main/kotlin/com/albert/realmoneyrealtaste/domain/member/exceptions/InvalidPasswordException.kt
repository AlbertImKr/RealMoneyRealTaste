package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 비밀번호가 일치하지 않는 경우
 */
class InvalidPasswordException(message: String) : MemberDomainException(message)
