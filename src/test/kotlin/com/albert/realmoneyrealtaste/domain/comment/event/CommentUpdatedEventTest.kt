package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentUpdatedEventTest {

    @Test
    fun `construct - success - creates CommentUpdatedEvent with valid parameters`() {
        val commentId = 1L
        val postId = 10L
        val authorMemberId = 100L
        val updatedAt = LocalDateTime.now()

        val event = CommentUpdatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            updatedAt = updatedAt
        )

        assertEquals(commentId, event.commentId)
        assertEquals(postId, event.postId)
        assertEquals(authorMemberId, event.authorMemberId)
        assertEquals(updatedAt, event.updatedAt)
    }
}
