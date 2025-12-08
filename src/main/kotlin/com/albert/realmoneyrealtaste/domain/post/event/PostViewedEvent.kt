package com.albert.realmoneyrealtaste.domain.post.event

data class PostViewedEvent(
    override val postId: Long,
    val viewerMemberId: Long,
    val authorMemberId: Long,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostViewedEvent = this.copy(postId = postId)
}
