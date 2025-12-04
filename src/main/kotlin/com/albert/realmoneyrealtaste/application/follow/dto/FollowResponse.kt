package com.albert.realmoneyrealtaste.application.follow.dto

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import java.time.LocalDateTime

/**
 * 팔로우 관계 응답 DTO
 */
data class FollowResponse(
    val followId: Long,
    val followingId: Long,
    val followingProfileImageId: Long,
    val followingNickname: String,
    val followerId: Long,
    val followerNickname: String,
    val followerProfileImageId: Long,
    val status: FollowStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        /**
         * Follow 엔티티를 FollowResponse로 변환
         * FollowRelationship에 닉네임이 포함되어 있으므로 직접 매핑
         */
        fun from(follow: Follow): FollowResponse {
            return FollowResponse(
                followId = follow.requireId(),
                followerId = follow.relationship.followerId,
                followerNickname = follow.relationship.followerNickname,
                followerProfileImageId = follow.relationship.followerProfileImageId,
                followingId = follow.relationship.followingId,
                followingNickname = follow.relationship.followingNickname,
                followingProfileImageId = follow.relationship.followingProfileImageId,
                status = follow.status,
                createdAt = follow.createdAt,
                updatedAt = follow.updatedAt
            )
        }
    }
}
