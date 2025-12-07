package com.albert.realmoneyrealtaste.domain.member.event

import java.time.LocalDateTime

/**
 * 비밀번호가 변경된 도메인 이벤트
 */
data class PasswordChangedDomainEvent(
    override val memberId: Long,
    val email: String,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
) : MemberDomainEvent {
    override fun withMemberId(memberId: Long): MemberDomainEvent = this.copy(memberId = memberId)
}
