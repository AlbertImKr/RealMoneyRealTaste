package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateRequestException
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentUpdateRequestTest {

    @Test
    fun `create - success - creates multiple instances with different values`() {
        val request1 = CommentUpdateRequest(1L, "첫 번째 댓글", 10L)
        val request2 = CommentUpdateRequest(2L, "두 번째 댓글", 20L)
        val request3 = CommentUpdateRequest(3L, "세 번째 댓글", 30L)

        assertAll(
            { assertEquals(1L, request1.commentId) },
            { assertEquals("첫 번째 댓글", request1.content) },
            { assertEquals(10L, request1.memberId) },
            { assertEquals(2L, request2.commentId) },
            { assertEquals("두 번째 댓글", request2.content) },
            { assertEquals(20L, request2.memberId) },
            { assertEquals(3L, request3.commentId) },
            { assertEquals("세 번째 댓글", request3.content) },
            { assertEquals(30L, request3.memberId) }
        )
    }

    @Test
    fun `create - failure - throws InvalidCommentIdException when commentId is zero`() {
        assertFailsWith<CommentUpdateRequestException.InvalidCommentIdException> {
            CommentUpdateRequest(
                commentId = 0L,
                content = "내용",
                memberId = 1L
            )
        }.let {
            assertEquals("댓글 ID는 양수여야 합니다: 0", it.message)
        }
    }

    @Test
    fun `create - failure - throws InvalidCommentIdException when commentId is negative`() {
        assertFailsWith<CommentUpdateRequestException.InvalidCommentIdException> {
            CommentUpdateRequest(
                commentId = -1L,
                content = "내용",
                memberId = 1L
            )
        }.let {
            assertEquals("댓글 ID는 양수여야 합니다: -1", it.message)
        }
    }

    @Test
    fun `create - failure - throws InvalidCommentIdException when memberId is zero`() {
        assertFailsWith<CommentUpdateRequestException.InvalidCommentIdException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = "내용",
                memberId = 0L
            )
        }.let {
            assertEquals("회원 ID는 양수여야 합니다: 0", it.message)
        }
    }

    @Test
    fun `create - failure - throws InvalidCommentIdException when memberId is negative`() {
        assertFailsWith<CommentUpdateRequestException.InvalidCommentIdException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = "내용",
                memberId = -5L
            )
        }.let {
            assertEquals("회원 ID는 양수여야 합니다: -5", it.message)
        }
    }

    @Test
    fun `create - failure - throws EmptyContentException when content is empty string`() {
        assertFailsWith<CommentUpdateRequestException.EmptyContentException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = "",
                memberId = 2L
            )
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws EmptyContentException when content is whitespace only`() {
        assertFailsWith<CommentUpdateRequestException.EmptyContentException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = "   ",
                memberId = 2L
            )
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws EmptyContentException when content is tabs and newlines`() {
        assertFailsWith<CommentUpdateRequestException.EmptyContentException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = "\t\n\r ",
                memberId = 2L
            )
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws ExceedContentLengthException when content exceeds max length`() {
        val longContent = "a".repeat(501)

        assertFailsWith<CommentUpdateRequestException.ExceedContentLengthException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = longContent,
                memberId = 2L
            )
        }.let {
            assertEquals("댓글 내용은 500자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws ExceedContentLengthException when content is much longer than max`() {
        val veryLongContent = "a".repeat(1000)

        assertFailsWith<CommentUpdateRequestException.ExceedContentLengthException> {
            CommentUpdateRequest(
                commentId = 1L,
                content = veryLongContent,
                memberId = 2L
            )
        }.let {
            assertEquals("댓글 내용은 500자를 초과할 수 없습니다.", it.message)
        }
    }
}
