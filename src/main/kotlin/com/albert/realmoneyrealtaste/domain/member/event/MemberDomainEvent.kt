package com.albert.realmoneyrealtaste.domain.member.event

import com.albert.realmoneyrealtaste.domain.common.DomainEvent

interface MemberDomainEvent : DomainEvent {
    val memberId: Long

    fun withMemberId(memberId: Long): MemberDomainEvent
}
