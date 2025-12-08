package com.albert.realmoneyrealtaste.domain.member.event

import java.time.LocalDateTime

/**
 * 회원 프로필이 업데이트된 도메인 이벤트
 */
data class MemberProfileUpdatedDomainEvent(
    override val memberId: Long,
    val email: String,
    val updatedFields: List<String>,
    val nickname: String? = null,
    val imageId: Long? = null,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
) : MemberDomainEvent {
    override fun withMemberId(memberId: Long): MemberProfileUpdatedDomainEvent = this.copy(memberId = memberId)
}
