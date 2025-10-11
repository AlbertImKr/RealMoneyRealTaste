package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 이미 활성화된 회원을 활성화하려는 경우
 */
class AlreadyActivatedException : MemberDomainException("이미 활성화된 회원입니다")
