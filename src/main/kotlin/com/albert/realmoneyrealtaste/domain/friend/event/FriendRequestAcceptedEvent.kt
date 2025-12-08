package com.albert.realmoneyrealtaste.domain.friend.event

/**
 * 친구 요청 수락 이벤트
 */
data class FriendRequestAcceptedEvent(
    override val friendshipId: Long,
    val fromMemberId: Long,
    val toMemberId: Long,
) : FriendDomainEvent {
    override fun withFriendshipId(friendshipId: Long): FriendDomainEvent = copy(friendshipId = friendshipId)
}
