package com.albert.realmoneyrealtaste.domain.comment.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentAuthorTest {

    @Test
    fun `create - success - creates comment author with valid parameters`() {
        val commentAuthor = CommentAuthor(1L, "맛집탐험가")

        assertEquals(1L, commentAuthor.memberId)
        assertEquals("맛집탐험가", commentAuthor.nickname)
    }

    @Test
    fun `create - success - creates comment author with 20 character nickname`() {
        val nickname = "12345678901234567890" // 20자
        val commentAuthor = CommentAuthor(1L, nickname)

        assertEquals(nickname, commentAuthor.nickname)
        assertEquals(20, commentAuthor.nickname.length)
    }

    @Test
    fun `create - success - creates comment author with single character nickname`() {
        val commentAuthor = CommentAuthor(1L, "김")

        assertEquals("김", commentAuthor.nickname)
    }

    @Test
    fun `create - success - creates comment author with Korean characters`() {
        val commentAuthor = CommentAuthor(1L, "한글닉네임")

        assertEquals("한글닉네임", commentAuthor.nickname)
    }

    @Test
    fun `create - success - creates comment author with alphanumeric nickname`() {
        val commentAuthor = CommentAuthor(1L, "User123")

        assertEquals("User123", commentAuthor.nickname)
    }

    @Test
    fun `create - success - creates comment author with special characters`() {
        val commentAuthor = CommentAuthor(1L, "맛집탐험가_2024!")

        assertEquals("맛집탐험가_2024!", commentAuthor.nickname)
    }

    @Test
    fun `create - failure - throws exception when memberId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(0L, "닉네임")
        }.let {
            assertEquals("회원 ID는 정수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(-1L, "닉네임")
        }.let {
            assertEquals("회원 ID는 정수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname is empty`() {
        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(1L, "")
        }.let {
            assertEquals("닉네임은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname is blank`() {
        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(1L, "   ")
        }.let {
            assertEquals("닉네임은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname contains only whitespace`() {
        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(1L, "\t\n\r")
        }.let {
            assertEquals("닉네임은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname exceeds 20 characters`() {
        val longNickname = "123456789012345678901" // 21자

        assertFailsWith<IllegalArgumentException> {
            CommentAuthor(1L, longNickname)
        }.let {
            assertEquals("닉네임은 20자를 초과할 수 없습니다.", it.message)
        }
    }
}
