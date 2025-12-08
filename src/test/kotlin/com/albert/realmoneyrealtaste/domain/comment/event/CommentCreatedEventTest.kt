package com.albert.realmoneyrealtaste.domain.comment.event

import java.time.LocalDateTime
import kotlin.test.Test

class CommentCreatedEventTest {

    @Test
    fun `construct - success - creates CommentCreatedEvent with valid parameters`() {
        val commentId = 1L
        val postId = 10L
        val authorMemberId = 100L
        val parentCommentId = 5L
        val createdAt = LocalDateTime.now()

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = null,
            createdAt = createdAt
        )

        kotlin.test.assertEquals(commentId, event.commentId)
        kotlin.test.assertEquals(postId, event.postId)
        kotlin.test.assertEquals(authorMemberId, event.authorMemberId)
        kotlin.test.assertEquals(parentCommentId, event.parentCommentId)
        kotlin.test.assertEquals(createdAt, event.createdAt)
    }
}
