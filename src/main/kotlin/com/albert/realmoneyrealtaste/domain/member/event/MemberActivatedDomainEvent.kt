package com.albert.realmoneyrealtaste.domain.member.event

import java.time.LocalDateTime

/**
 * 회원이 활성화된 도메인 이벤트
 */
data class MemberActivatedDomainEvent(
    val memberId: Long,
    val email: String,
    val nickname: String,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
