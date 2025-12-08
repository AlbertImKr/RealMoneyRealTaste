package com.albert.realmoneyrealtaste.domain.member.event

import java.time.LocalDateTime

/**
 * 회원 비활성화 도메인 이벤트
 */
data class MemberDeactivatedDomainEvent(
    override val memberId: Long,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
) : MemberDomainEvent {
    override fun withMemberId(memberId: Long): MemberDeactivatedDomainEvent = this.copy(memberId = memberId)
}
