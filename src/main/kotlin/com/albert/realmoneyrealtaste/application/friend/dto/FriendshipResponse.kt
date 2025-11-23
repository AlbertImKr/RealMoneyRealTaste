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
    val friendMemberId: Long,
    val friendNickname: String,
    val status: FriendshipStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // 템플릿용 추가 필드
    val id: Long = friendMemberId, // 템플릿에서 사용할 ID
    val nickname: String = friendNickname, // 템플릿에서 사용할 닉네임
    val mutualFriendsCount: Int = 0, // 상호 친구 수 (기본값)
    val friendSince: LocalDateTime = createdAt, // 친구가 된 날짜
    val profileImageUrl: String? = null, // 프로필 이미지 URL
) {
    companion object {

        fun from(
            friendship: Friendship,
            friendNickname: String,
            mutualFriendsCount: Int = 0,
            profileImageUrl: String? = null,
        ): FriendshipResponse {
            return FriendshipResponse(
                friendshipId = friendship.requireId(),
                memberId = friendship.relationShip.memberId,
                friendMemberId = friendship.relationShip.friendMemberId,
                friendNickname = friendNickname,
                status = friendship.status,
                createdAt = friendship.createdAt,
                updatedAt = friendship.updatedAt,
                mutualFriendsCount = mutualFriendsCount,
                profileImageUrl = profileImageUrl
            )
        }
    }
}
