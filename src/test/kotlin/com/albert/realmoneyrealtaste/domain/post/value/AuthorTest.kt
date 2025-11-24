package com.albert.realmoneyrealtaste.domain.post.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthorTest {
    private val TEST_INTRODUCTION = "안녕하세요. 테스트 작성자입니다."

    @Test
    fun `create - success - creates author with valid parameters`() {
        val author = Author(1L, "작성자", TEST_INTRODUCTION)

        assertEquals(1L, author.memberId)
        assertEquals("작성자", author.nickname)
        assertEquals(TEST_INTRODUCTION, author.introduction)
    }

    @Test
    fun `create - failure - throws exception when nickname is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Author(1L, "", TEST_INTRODUCTION)
        }.let {
            assertEquals("작성자 닉네임은 필수입니다.", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname exceeds max length`() {
        val longNickname = "a".repeat(21)

        assertFailsWith<IllegalArgumentException> {
            Author(1L, longNickname, TEST_INTRODUCTION)
        }.let {
            assertEquals("닉네임은 20자 이내여야 합니다.", it.message)
        }
    }
}
