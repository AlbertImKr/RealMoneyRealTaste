package com.albert.realmoneyrealtaste.application.follow.event

/**
 * 팔로우 시작 이벤트
 */
data class FollowStartedEvent(
    val followId: Long,
    val followerId: Long,
    val followingId: Long,
)
