package com.albert.realmoneyrealtaste.domain.comment.event

import kotlin.test.Test

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

        kotlin.test.assertEquals(commentId, event.commentId)
        kotlin.test.assertEquals(postId, event.postId)
        kotlin.test.assertEquals(authorMemberId, event.authorMemberId)
        kotlin.test.assertEquals(deletedAt, event.deletedAt)
    }

}
