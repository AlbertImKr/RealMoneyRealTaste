package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 생성 이벤트
 */
data class CommentCreatedEvent(
    override val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    val parentCommentId: Long?,
    val parentCommentAuthorId: Long?,
    val createdAt: LocalDateTime,
) : CommentDomainEvent {
    override fun withCommentId(commentId: Long): CommentCreatedEvent = copy(commentId = commentId)
}
