package com.albert.realmoneyrealtaste.domain.post.event

data class PostDeletedEvent(
    val postId: Long,
    val authorMemberId: Long,
)
