package com.albert.realmoneyrealtaste.application.follow.event

data class UnfollowedEvent(
    val followId: Long,
    val followerId: Long,
    val followingId: Long,
)
