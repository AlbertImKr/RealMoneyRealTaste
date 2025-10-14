package com.albert.realmoneyrealtaste.domain.member.exceptions

class MemberNotActiveException(message: String = "회원이 활성 상태가 아닙니다.") : MemberDomainException(message)
