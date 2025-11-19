package com.albert.realmoneyrealtaste.application.follow.dto

data class FollowCreateRequest(
    val followerId: Long,
    val followingId: Long,
)
