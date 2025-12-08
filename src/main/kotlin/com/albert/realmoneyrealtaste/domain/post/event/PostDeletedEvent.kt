package com.albert.realmoneyrealtaste.domain.post.event

import java.time.LocalDateTime

data class PostDeletedEvent(
    override val postId: Long,
    val authorMemberId: Long,
    val occurredAt: LocalDateTime,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostDeletedEvent = this.copy(postId = postId)
}
