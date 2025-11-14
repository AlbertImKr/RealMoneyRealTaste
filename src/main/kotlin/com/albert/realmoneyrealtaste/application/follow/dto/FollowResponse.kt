package com.albert.realmoneyrealtaste.application.follow.dto

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import java.time.LocalDateTime

/**
 * 팔로우 관계 응답 DTO
 */
data class FollowResponse(
    val followId: Long,
    val followerId: Long,
    val followerNickname: String,
    val followingId: Long,
    val followingNickname: String,
    val status: FollowStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(follow: Follow, followerNickname: String, followingNickname: String): FollowResponse {
            return FollowResponse(
                followId = follow.requireId(),
                followerId = follow.relationship.followerId,
                followerNickname = followerNickname,
                followingId = follow.relationship.followingId,
                followingNickname = followingNickname,
                status = follow.status,
                createdAt = follow.createdAt,
                updatedAt = follow.updatedAt
            )
        }
    }
}
