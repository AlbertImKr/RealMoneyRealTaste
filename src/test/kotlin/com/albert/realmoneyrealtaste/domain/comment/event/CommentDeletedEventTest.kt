package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentDeletedEventTest {

    @Test
    fun `construct - success - creates CommentDeletedEvent with valid parameters`() {
        val commentId = 1L
        val postId = 10L
        val parentCommentId = 123L
        val authorMemberId = 100L
        val deletedAt = LocalDateTime.now()

        val event = CommentDeletedEvent(
            commentId = commentId,
            parentCommentId = parentCommentId,
            postId = postId,
            authorMemberId = authorMemberId,
            deletedAt = deletedAt
        )

        assertEquals(commentId, event.commentId)
        assertEquals(parentCommentId, event.parentCommentId)
        assertEquals(postId, event.postId)
        assertEquals(authorMemberId, event.authorMemberId)
        assertEquals(deletedAt, event.deletedAt)
    }

    @Test
    fun `construct - success - creates CommentDeletedEvent with null parentCommentId when parentCommentId is not provided`() {
        val commentId = 1L
        val postId = 10L
        val authorMemberId = 100L
        val deletedAt = LocalDateTime.now()

        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            deletedAt = deletedAt,
            parentCommentId = null
        )

        assertEquals(commentId, event.commentId)
        assertEquals(null, event.parentCommentId)
        assertEquals(postId, event.postId)
        assertEquals(authorMemberId, event.authorMemberId)
        assertEquals(deletedAt, event.deletedAt)
    }
}
