package com.albert.realmoneyrealtaste.application.friend.dto

import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import java.time.LocalDateTime

/**
 * 친구 관계 응답 DTO
 */
data class FriendshipResponse(
    val friendshipId: Long,
    val memberId: Long,
    val memberNickname: String,
    val friendMemberId: Long,
    val friendNickname: String,
    val status: FriendshipStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(friendship: Friendship, memberNickname: String, friendNickname: String): FriendshipResponse {
            return FriendshipResponse(
                friendshipId = friendship.requireId(),
                memberId = friendship.relationShip.memberId,
                memberNickname = memberNickname,
                friendMemberId = friendship.relationShip.friendMemberId,
                friendNickname = friendNickname,
                status = friendship.status,
                createdAt = friendship.createdAt,
                updatedAt = friendship.updatedAt
            )
        }
    }
}
