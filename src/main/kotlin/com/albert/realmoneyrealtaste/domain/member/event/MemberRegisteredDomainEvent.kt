package com.albert.realmoneyrealtaste.domain.member.event

import java.time.LocalDateTime

/**
 * 회원이 등록된 도메인 이벤트
 * 순수 비즈니스 사실만 포함
 */
data class MemberRegisteredDomainEvent(
    override val memberId: Long,
    val email: String,
    val nickname: String,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
) : MemberDomainEvent {
    override fun withMemberId(memberId: Long): MemberRegisteredDomainEvent = this.copy(memberId = memberId)
}
