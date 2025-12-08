package com.albert.realmoneyrealtaste.domain.post.event

data class PostHeartAddedEvent(
    override val postId: Long,
    val memberId: Long,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostHeartAddedEvent = this.copy(postId = postId)
}
