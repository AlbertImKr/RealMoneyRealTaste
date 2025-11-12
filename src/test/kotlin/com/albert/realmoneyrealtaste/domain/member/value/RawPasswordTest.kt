package com.albert.realmoneyrealtaste.domain.member.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RawPasswordTest {

    @Test
    fun `constructor - success - creates raw password with valid format`() {
        RawPassword("Valid1!34")
        RawPassword("Another$2")
        RawPassword("StrongPass3#")
    }

    @Test
    fun `constructor - failure - throws exception when password is blank`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("")
        }.let {
            assertEquals("비밀번호는 필수입니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password is too short`() {
        val shortPassword = "Short1!" // 7 characters

        assertFailsWith<IllegalArgumentException> {
            RawPassword(shortPassword)
        }.let {
            assertEquals("비밀번호는 8 ~ 20 자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password is too long`() {
        val longPassword = "ThisPasswordIsWayToo!" // 21 characters

        assertFailsWith<IllegalArgumentException> {
            RawPassword(longPassword)
        }.let {
            assertEquals("비밀번호는 8 ~ 20 자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password contains no digit`() {
        val passwordWithoutDigit = "NoDigitsHere!"

        assertFailsWith<IllegalArgumentException> {
            RawPassword(passwordWithoutDigit)
        }.let {
            assertEquals("비밀번호에는 최소한 하나의 숫자가 포함되어야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password contains no lowercase`() {
        val passwordWithoutLowercase = "NOLOWERCASE1!"

        assertFailsWith<IllegalArgumentException> {
            RawPassword(passwordWithoutLowercase)
        }.let {
            assertEquals("비밀번호에는 최소한 하나의 소문자가 포함되어야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password contains no uppercase`() {
        val passwordWithoutUppercase = "nouppercase1!"

        assertFailsWith<IllegalArgumentException> {
            RawPassword(passwordWithoutUppercase)
        }.let {
            assertEquals("비밀번호에는 최소한 하나의 대문자가 포함되어야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password contains no special character`() {
        val passwordWithoutSpecialChar = "NoSpecialChar1"

        assertFailsWith<IllegalArgumentException> {
            RawPassword(passwordWithoutSpecialChar)
        }.let {
            assertEquals("비밀번호에는 최소한 하나의 특수문자가 포함되어야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when password contains invalid special character`() {
        val passwordWithInvalidChar = "InvalidChar1?"

        assertFailsWith<IllegalArgumentException> {
            RawPassword(passwordWithInvalidChar)
        }.let {
            assertEquals(
                "비밀번호에 허용되지 않는 특수문자가 포함되어 있습니다. 허용되는 특수문자: ${RawPassword.ALLOWED_SPECIAL_CHARS}",
                it.message
            )
        }
    }
}
