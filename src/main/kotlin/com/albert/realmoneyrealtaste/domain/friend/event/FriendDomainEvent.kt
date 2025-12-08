package com.albert.realmoneyrealtaste.domain.friend.event

import com.albert.realmoneyrealtaste.domain.common.DomainEvent

interface FriendDomainEvent : DomainEvent {
    val friendshipId: Long

    fun withFriendshipId(friendshipId: Long): FriendDomainEvent
}
