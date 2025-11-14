package com.albert.realmoneyrealtaste.domain.friend.event

/**
 * 친구 관계 해제 이벤트
 */
data class FriendshipTerminatedEvent(
    val memberId: Long,
    val friendMemberId: Long,
)
