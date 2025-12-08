package com.albert.realmoneyrealtaste.domain.post.event

data class PostDeletedEvent(
    override val postId: Long,
    val authorMemberId: Long,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostDeletedEvent = this.copy(postId = postId)
}
