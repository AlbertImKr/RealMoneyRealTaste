package com.albert.realmoneyrealtaste.domain.post.event

data class PostCreatedEvent(
    override val postId: Long,
    val authorMemberId: Long,
    val restaurantName: String,
) : PostDomainEvent {
    override fun withPostId(postId: Long): PostCreatedEvent = this.copy(postId = postId)
}
