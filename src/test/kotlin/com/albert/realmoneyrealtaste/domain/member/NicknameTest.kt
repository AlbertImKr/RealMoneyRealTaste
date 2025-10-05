package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NicknameTest {

    @Test
    fun `constructor - success - creates nickname with valid value`() {
        val nickNameValue = "ValidName"

        val nickname = Nickname(nickNameValue)

        assertEquals(nickNameValue, nickname.value)
    }

    @Test
    fun `constructor - failure - throws exception when nickname is empty`() {
        val emptyNickname = "   "

        assertFailsWith<IllegalArgumentException> {
            Nickname(emptyNickname)
        }.let {
            assertEquals("닉네임은 필수입니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when nickname is too short`() {
        val shortNickname = "A"

        assertFailsWith<IllegalArgumentException> {
            Nickname(shortNickname)
        }.let {
            assertEquals("닉네임은 2-20자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when nickname is too long`() {
        val longNickname = "A".repeat(21)

        assertFailsWith<IllegalArgumentException> {
            Nickname(longNickname)
        }.let {
            assertEquals("닉네임은 2-20자 사이여야 합니다", it.message)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Invalid!", "Name With Spaces", "Name@Domain", "Name#1", "Name$"
        ]
    )
    fun `constructor - failure - throws exception when nickname contains special characters`(invalidNickname: String) {
        assertFailsWith<IllegalArgumentException> {
            Nickname(invalidNickname)
        }.let {
            assertEquals("닉네임은 한글, 영문, 숫자만 사용 가능합니다", it.message)
        }
    }
}
