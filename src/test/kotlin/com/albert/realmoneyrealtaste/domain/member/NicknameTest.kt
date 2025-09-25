package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NicknameTest {

    @Test
    fun `test valid nickname`() {
        val nickNameValue = "ValidName"

        val nickname = Nickname(nickNameValue)

        assertEquals(nickNameValue, nickname.value)
    }

    @Test
    fun `test invalid nickname - empty`() {
        val emptyNickname = "   "

        assertFailsWith<IllegalArgumentException> {
            Nickname(emptyNickname)
        }.let {
            assertEquals("닉네임은 필수입니다", it.message)
        }
    }

    @Test
    fun `test invalid nickname - too short`() {
        val shortNickname = "A"

        assertFailsWith<IllegalArgumentException> {
            Nickname(shortNickname)
        }.let {
            assertEquals("닉네임은 2-20자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `test invalid nickname - too long`() {
        val longNickname = "A".repeat(21)

        assertFailsWith<IllegalArgumentException> {
            Nickname(longNickname)
        }.let {
            assertEquals("닉네임은 2-20자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `test invalid nickname - special characters`() {
        val shortNickname = "A".repeat(21)

        assertFailsWith<IllegalArgumentException> {
            Nickname(shortNickname)
        }.let {
            assertEquals("닉네임은 2-20자 사이여야 합니다", it.message)
        }
    }
}
