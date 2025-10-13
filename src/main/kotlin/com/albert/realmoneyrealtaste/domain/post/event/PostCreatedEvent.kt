package com.albert.realmoneyrealtaste.domain.post.event

data class PostCreatedEvent(
    val postId: Long,
    val authorMemberId: Long,
    val restaurantName: String,
)
