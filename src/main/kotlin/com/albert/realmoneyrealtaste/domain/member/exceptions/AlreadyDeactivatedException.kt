package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 이미 비활성화된 회원을 비활성화하려는 경우
 */
class AlreadyDeactivatedException : MemberDomainException("이미 비활성화된 회원입니다")
