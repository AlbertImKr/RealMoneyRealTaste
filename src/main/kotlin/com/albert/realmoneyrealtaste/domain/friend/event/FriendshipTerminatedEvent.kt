package com.albert.realmoneyrealtaste.domain.friend.event

/**
 * 친구 관계 해제 이벤트
 */
data class FriendshipTerminatedEvent(
    override val friendshipId: Long,
    val memberId: Long,
    val friendMemberId: Long,
) : FriendDomainEvent {
    override fun withFriendshipId(friendshipId: Long): FriendDomainEvent = copy(friendshipId = friendshipId)
}

