package com.albert.realmoneyrealtaste.domain.comment.value

import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentContentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentContentTest {

    @Test
    fun `create - success - creates comment content with valid text`() {
        val commentContent = CommentContent("좋은 댓글입니다!")

        assertEquals("좋은 댓글입니다!", commentContent.text)
    }

    @Test
    fun `create - success - creates comment content with single character`() {
        val commentContent = CommentContent("좋")

        assertEquals("좋", commentContent.text)
    }

    @Test
    fun `create - success - accepts text at max length`() {
        val maxLengthText = "a".repeat(500)
        val commentContent = CommentContent(maxLengthText)

        assertEquals(500, commentContent.text.length)
        assertEquals(maxLengthText, commentContent.text)
    }

    @Test
    fun `create - failure - throws exception when text is empty`() {
        assertFailsWith<InvalidCommentContentException> {
            CommentContent("")
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when text is blank`() {
        assertFailsWith<InvalidCommentContentException> {
            CommentContent("   ")
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when text contains only whitespace`() {
        assertFailsWith<InvalidCommentContentException> {
            CommentContent("\t\n\r")
        }.let {
            assertEquals("댓글 내용은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when text exceeds max length`() {
        val longText = "a".repeat(501)

        assertFailsWith<InvalidCommentContentException> {
            CommentContent(longText)
        }.let {
            assertEquals("댓글 내용은 500자를 초과할 수 없습니다.", it.message)
        }
    }

    @Test
    fun `extractMentions - success - returns empty list when no mentions`() {
        val commentContent = CommentContent("일반적인 댓글 내용입니다.")

        val mentions = commentContent.extractMentions()

        assertEquals(emptyList(), mentions)
    }

    @Test
    fun `extractMentions - success - extracts single mention`() {
        val commentContent = CommentContent("@맛집탐험가 님의 의견에 동감합니다!")

        val mentions = commentContent.extractMentions()

        assertEquals(listOf("맛집탐험가"), mentions)
    }

    @Test
    fun `extractMentions - success - extracts multiple mentions`() {
        val commentContent = CommentContent("@user1 @맛집러버 @foodie123 모두 좋은 의견이네요!")

        val mentions = commentContent.extractMentions()

        assertEquals(listOf("user1", "맛집러버", "foodie123"), mentions)
    }

    @Test
    fun `extractMentions - success - removes duplicate mentions`() {
        val commentContent = CommentContent("@맛집탐험가 님과 @맛집탐험가 님의 의견이 같네요.")

        val mentions = commentContent.extractMentions()

        assertEquals(listOf("맛집탐험가"), mentions)
    }

    @Test
    fun `extractMentions - success - ignores invalid mention formats`() {
        val commentContent = CommentContent("@a (너무 짧음) @verylongusernameover20chars1 (너무 김) @특수문자! 무시")

        val mentions = commentContent.extractMentions()

        assertEquals(emptyList(), mentions)
    }
}
