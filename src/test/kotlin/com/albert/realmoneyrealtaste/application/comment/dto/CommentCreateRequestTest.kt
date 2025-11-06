package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreateRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentCreateRequestTest {

    @Test
    fun `construct - success - creates CommentCreateRequest with valid parameters`() {
        val postId = 10L
        val memberId = 100L
        val content = "This is a test comment."

        val request = CommentCreateRequest(
            postId = postId,
            memberId = memberId,
            content = content
        )

        assertEquals(postId, request.postId)
        assertEquals(memberId, request.memberId)
        assertEquals(content, request.content)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when postId is non-positive`() {
        val exception = assertFailsWith<CommentCreateRequestException.InvalidPostIdException> {
            CommentCreateRequest(
                postId = 0L,
                memberId = 100L,
                content = "This is a test comment."
            )
        }

        assertEquals("게시글 ID는 양수여야 합니다: 0", exception.message)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when memberId is non-positive`() {
        val exception = assertFailsWith<CommentCreateRequestException.InvalidMemberIdException> {
            CommentCreateRequest(
                postId = 10L,
                memberId = -1L,
                content = "This is a test comment."
            )
        }

        assertEquals("회원 ID는 양수여야 합니다: -1", exception.message)
    }

    @Test
    fun `construct - failure - throws IllegalArgumentException when content is blank`() {
        val exception = assertFailsWith<CommentCreateRequestException.EmptyContentException> {
            CommentCreateRequest(
                postId = 10L,
                memberId = 100L,
                content = "   "
            )
        }

        assertEquals("댓글 내용은 필수입니다.", exception.message)
    }
}
