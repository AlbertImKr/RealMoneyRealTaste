package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 삭제 이벤트
 */
data class CommentDeletedEvent(
    override val commentId: Long,
    val parentCommentId: Long?,
    val postId: Long,
    val authorMemberId: Long,
    val deletedAt: LocalDateTime,
) : CommentDomainEvent {
    override fun withCommentId(commentId: Long): CommentDeletedEvent = copy(commentId = commentId)
}
