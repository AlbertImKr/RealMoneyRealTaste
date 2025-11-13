package com.albert.realmoneyrealtaste.domain.comment.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentMentionTest {

    @Test
    fun `create - success - creates mention with valid parameters`() {
        val mention = CommentMention(1L, "맛집탐험가", 0, 6)

        assertEquals(1L, mention.memberId)
        assertEquals("맛집탐험가", mention.nickname)
        assertEquals(0, mention.startPosition)
        assertEquals(6, mention.endPosition)
    }

    @Test
    fun `create - success - creates mention with maximum nickname length`() {
        val nickname = "a".repeat(20) // 최대 20자
        val mention = CommentMention(1L, nickname, 0, 21)

        assertEquals(nickname, mention.nickname)
    }

    @Test
    fun `create - failure - throws exception when memberId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(0L, "닉네임", 0, 3)
        }.let {
            assertEquals("유효하지 않은 멤버 ID입니다.: 0", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(-1L, "닉네임", 0, 3)
        }.let {
            assertEquals("유효하지 않은 멤버 ID입니다.: -1", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, "", 0, 1)
        }.let {
            assertEquals("닉네임은 비어 있을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname is blank`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, "   ", 0, 4)
        }.let {
            assertEquals("닉네임은 비어 있을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname exceeds max length`() {
        val longNickname = "a".repeat(21) // 21자

        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, longNickname, 0, 22)
        }.let {
            assertEquals("닉네임은 20 자를 초과할 수 없습니다.: 21", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when startPosition is negative`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, "닉네임", -1, 3)
        }.let {
            assertEquals("시작 위치는 0 이상이어야 합니다.: -1", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when endPosition equals startPosition`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, "닉네임", 5, 5)
        }.let {
            assertEquals("끝 위치는 시작 위치보다 커야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when endPosition is less than startPosition`() {
        assertFailsWith<IllegalArgumentException> {
            CommentMention(1L, "닉네임", 10, 5)
        }.let {
            assertEquals("끝 위치는 시작 위치보다 커야 합니다.", it.message)
        }
    }

    @Test
    fun `length - success - returns correct length`() {
        val mention = CommentMention(1L, "맛집탐험가", 0, 6)

        assertEquals(6, mention.length())
    }

    @Test
    fun `length - success - returns correct length for different positions`() {
        val mention = CommentMention(1L, "user", 10, 15)

        assertEquals(5, mention.length())
    }

    @Test
    fun `contains - success - returns true when position is within range`() {
        val mention = CommentMention(1L, "닉네임", 5, 10)

        assertTrue(mention.contains(5))
        assertTrue(mention.contains(7))
        assertTrue(mention.contains(9))
    }

    @Test
    fun `contains - success - returns false when position is outside range`() {
        val mention = CommentMention(1L, "닉네임", 5, 10)

        assertFalse(mention.contains(4))
        assertFalse(mention.contains(10))
        assertFalse(mention.contains(11))
    }

    @Test
    fun `overlaps - success - returns true when mentions overlap`() {
        val mention1 = CommentMention(1L, "user1", 0, 6)
        val mention2 = CommentMention(2L, "user2", 3, 9)

        assertTrue(mention1.overlaps(mention2))
        assertTrue(mention2.overlaps(mention1))
    }

    @Test
    fun `overlaps - success - returns false when mentions do not overlap`() {
        val mention1 = CommentMention(1L, "user1", 0, 5)
        val mention2 = CommentMention(2L, "user2", 5, 10)

        assertFalse(mention1.overlaps(mention2))
        assertFalse(mention2.overlaps(mention1))
    }

    @Test
    fun `overlaps - success - returns false when mentions are adjacent`() {
        val mention1 = CommentMention(1L, "user1", 0, 5)
        val mention2 = CommentMention(2L, "user2", 10, 15)

        assertFalse(mention1.overlaps(mention2))
    }

    @Test
    fun `adjustPosition - success - moves position by positive offset`() {
        val mention = CommentMention(1L, "닉네임", 5, 10)
        val adjusted = mention.adjustPosition(3)

        assertEquals(1L, adjusted.memberId)
        assertEquals("닉네임", adjusted.nickname)
        assertEquals(8, adjusted.startPosition)
        assertEquals(13, adjusted.endPosition)
    }

    @Test
    fun `adjustPosition - success - moves position by negative offset`() {
        val mention = CommentMention(1L, "닉네임", 10, 15)
        val adjusted = mention.adjustPosition(-3)

        assertEquals(1L, adjusted.memberId)
        assertEquals("닉네임", adjusted.nickname)
        assertEquals(7, adjusted.startPosition)
        assertEquals(12, adjusted.endPosition)
    }

    @Test
    fun `adjustPosition - success - zero offset returns equivalent mention`() {
        val mention = CommentMention(1L, "닉네임", 5, 10)
        val adjusted = mention.adjustPosition(0)

        assertEquals(mention.memberId, adjusted.memberId)
        assertEquals(mention.nickname, adjusted.nickname)
        assertEquals(mention.startPosition, adjusted.startPosition)
        assertEquals(mention.endPosition, adjusted.endPosition)
    }
}
