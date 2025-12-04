package com.albert.realmoneyrealtaste.application.friend.dto

import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.member.Member
import java.time.LocalDateTime

/**
 * 친구 관계 응답 DTO
 */
data class FriendshipResponse(
    val friendshipId: Long,
    val memberId: Long,
    val friendMemberId: Long,
    val friendNickname: String,
    val status: FriendshipStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val mutualFriendsCount: Int = 0,
    val memberNickname: String,
    val memberProfileImageId: Long,
    val friendProfileImageId: Long,
) {
    companion object {

        fun from(
            friendship: Friendship,
            member: Member,
            friend: Member,
        ): FriendshipResponse {
            return FriendshipResponse(
                friendshipId = friendship.requireId(),
                memberId = friendship.relationShip.memberId,
                friendMemberId = friendship.relationShip.friendMemberId,
                friendNickname = friend.nickname.value,
                status = friendship.status,
                createdAt = friendship.createdAt,
                updatedAt = friendship.updatedAt,
                memberNickname = member.nickname.value,
                memberProfileImageId = member.detail.imageId ?: 0L,
                friendProfileImageId = friend.detail.imageId ?: 0L,
            )
        }
    }
}
