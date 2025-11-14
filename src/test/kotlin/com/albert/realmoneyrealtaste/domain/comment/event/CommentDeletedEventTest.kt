package com.albert.realmoneyrealtaste.domain.comment.event

import kotlin.test.Test
import kotlin.test.assertEquals

class CommentDeletedEventTest {

    @Test
    fun `construct - success - creates CommentDeletedEvent with valid parameters`() {
        val commentId = 1L
        val postId = 10L
        val authorMemberId = 100L
        val deletedAt = java.time.LocalDateTime.now()

        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            deletedAt = deletedAt
        )

        assertEquals(commentId, event.commentId)
        assertEquals(postId, event.postId)
        assertEquals(authorMemberId, event.authorMemberId)
        assertEquals(deletedAt, event.deletedAt)
    }
}
