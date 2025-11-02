package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.ReplyCreateRequestException
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplyCreateRequestTest {

    @Test
    fun `construct - success - creates ReplyCreateRequest with valid parameters`() {
        val postId = 10L
        val memberId = 100L
        val content = "This is a test reply."
        val parentCommentId = 50L

        val request = ReplyCreateRequest(
            postId = postId,
            memberId = memberId,
            content = content,
            parentCommentId = parentCommentId
        )

        assert(request.postId == postId)
        assert(request.memberId == memberId)
        assert(request.content == content)
        assert(request.parentCommentId == parentCommentId)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when parentCommentId is non-positive`() {
        val exception = kotlin.test.assertFailsWith<ReplyCreateRequestException.InvalidParentCommentIdException> {
            ReplyCreateRequest(
                postId = 10L,
                memberId = 100L,
                content = "This is a test reply.",
                parentCommentId = 0L
            )
        }

        assertEquals("부모 댓글 ID는 양수여야 합니다: 0", exception.message)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when content is blank`() {
        val exception = kotlin.test.assertFailsWith<ReplyCreateRequestException.EmptyContentException> {
            ReplyCreateRequest(
                postId = 10L,
                memberId = 100L,
                content = "   ",
                parentCommentId = 50L
            )
        }

        assertEquals("댓글 내용은 필수입니다.", exception.message)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when postId is non-positive`() {
        val exception = kotlin.test.assertFailsWith<ReplyCreateRequestException.InvalidPostIdException> {
            ReplyCreateRequest(
                postId = -5L,
                memberId = 100L,
                content = "This is a test reply.",
                parentCommentId = 50L
            )
        }

        assertEquals("게시글 ID는 양수여야 합니다: -5", exception.message)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when memberId is non-positive`() {
        val exception = kotlin.test.assertFailsWith<ReplyCreateRequestException.InvalidMemberIdException> {
            ReplyCreateRequest(
                postId = 10L,
                memberId = 0L,
                content = "This is a test reply.",
                parentCommentId = 50L
            )
        }

        assertEquals("회원 ID는 양수여야 합니다: 0", exception.message)
    }
}
