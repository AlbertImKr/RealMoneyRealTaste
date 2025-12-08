package com.albert.realmoneyrealtaste.domain.post.event

import java.time.LocalDateTime

data class PostCreatedEvent(
    override val postId: Long,
    val authorMemberId: Long,
    val restaurantName: String,
    val occurredAt: LocalDateTime,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostCreatedEvent = this.copy(postId = postId)
}
