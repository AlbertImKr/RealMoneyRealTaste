package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 수정 이벤트
 */
data class CommentUpdatedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    val updatedAt: LocalDateTime,
)
