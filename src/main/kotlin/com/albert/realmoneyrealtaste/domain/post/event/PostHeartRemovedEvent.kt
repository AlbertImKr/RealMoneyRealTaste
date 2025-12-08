package com.albert.realmoneyrealtaste.domain.post.event

data class PostHeartRemovedEvent(
    override val postId: Long,
    val memberId: Long,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostHeartRemovedEvent = this.copy(postId = postId)
}
