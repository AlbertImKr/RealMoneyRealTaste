package com.albert.realmoneyrealtaste.domain.member.event

/**
 * 회원 비활성화 도메인 이벤트
 */
data class MemberDeactivatedDomainEvent(
    val memberId: Long,
)
