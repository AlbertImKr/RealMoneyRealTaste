package com.albert.realmoneyrealtaste.application.follow.dto

/**
 * 팔로우 통계 응답 DTO
 */
data class FollowStatsResponse(
    val memberId: Long,
    val followersCount: Long,
    val followingCount: Long,
)
