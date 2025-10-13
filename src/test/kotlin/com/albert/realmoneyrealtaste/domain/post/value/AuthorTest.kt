package com.albert.realmoneyrealtaste.domain.post.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class AuthorTest {

    @Test
    fun `create - success - creates author with valid parameters`() {
        val author = Author(1L, "작성자")

        assertEquals(1L, author.memberId)
        assertEquals("작성자", author.nickname)
    }

    @Test
    fun `create - failure - throws exception when nickname is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Author(1L, "")
        }.let {
            assertEquals("작성자 닉네임은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname exceeds max length`() {
        val longNickname = "a".repeat(21)

        assertFailsWith<IllegalArgumentException> {
            Author(1L, longNickname)
        }.let {
            assertEquals("닉네임은 20자 이내여야 합니다.", it.message)
        }
    }

    @Test
    fun `equals - success - returns true for same values`() {
        val author1 = Author(1L, "작성자")
        val author2 = Author(1L, "작성자")

        assertEquals(author1, author2)
    }

    @Test
    fun `equals - success - returns false for different values`() {
        val author1 = Author(1L, "작성자1")
        val author2 = Author(1L, "작성자2")

        assertNotEquals(author1, author2)
    }
}
