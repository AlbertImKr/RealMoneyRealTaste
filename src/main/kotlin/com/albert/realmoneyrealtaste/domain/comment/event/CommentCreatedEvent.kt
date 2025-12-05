package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime

/**
 * 댓글 생성 이벤트
 */
data class CommentCreatedEvent(
    val commentId: Long,
    val postId: Long,
    val authorMemberId: Long,
    val parentCommentId: Long?,
    val parentCommentAuthorId: Long?,
    val createdAt: LocalDateTime,
)
