package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 삭제 이벤트
 */
data class CommentDeletedEvent(
    val commentId: Long,
    val parentCommentId: Long?,
    val postId: Long,
    val authorMemberId: Long,
    val deletedAt: LocalDateTime,
)
