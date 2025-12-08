package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 수정 이벤트
 */
data class CommentUpdatedEvent(
    override val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    val updatedAt: LocalDateTime,
) : CommentDomainEvent {
    override fun withCommentId(commentId: Long): CommentUpdatedEvent = copy(commentId = commentId)
}
