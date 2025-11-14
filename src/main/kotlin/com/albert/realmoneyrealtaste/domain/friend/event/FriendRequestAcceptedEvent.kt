package com.albert.realmoneyrealtaste.domain.friend.event

/**
 * 친구 요청 수락 이벤트
 */
data class FriendRequestAcceptedEvent(
    val friendshipId: Long,
    val fromMemberId: Long,
    val toMemberId: Long,
)
