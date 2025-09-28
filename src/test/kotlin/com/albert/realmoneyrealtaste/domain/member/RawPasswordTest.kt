package com.albert.realmoneyrealtaste.domain.member

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RawPasswordTest {

    @Test
    fun `creates raw password`() {
        RawPassword("Valid1!34")
        RawPassword("Another$2")
        RawPassword("StrongPass3#")
    }

    @Test
    fun `blank raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("")
        }.let {
            assertEquals("비밀번호는 필수입니다", it.message)
        }
    }

    @Test
    fun `too short raw password`() {
        assertFailsWith<IllegalArgumentException> {
            // 7개 문자
            RawPassword("Short1!")
        }.let {
            assertEquals("비밀번호는 8자 이상 20자 이하여야 합니다", it.message)
        }
    }

    @Test
    fun `too long raw password`() {
        assertFailsWith<IllegalArgumentException> {
            // 21개 문자
            RawPassword("ThisPasswordIsWayToo!")
        }.let {
            assertEquals("비밀번호는 8자 이상 20자 이하여야 합니다", it.message)
        }
    }

    @Test
    fun `no digit in raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("NoDigitsHere!")
        }.let {
            assertEquals("비밀번호는 숫자를 포함해야 합니다", it.message)
        }
    }

    @Test
    fun `no lowercase in raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("NOLOWERCASE1!")
        }.let {
            assertEquals("비밀번호는 소문자를 포함해야 합니다", it.message)
        }
    }

    @Test
    fun `no uppercase in raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("nouppercase1!")
        }.let {
            assertEquals("비밀번호는 대문자를 포함해야 합니다", it.message)
        }
    }

    @Test
    fun `no special character in raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("NoSpecialChar1")
        }.let {
            assertEquals("비밀번호는 특수문자를 포함해야 합니다", it.message)
        }
    }

    @Test
    fun `contains invalid special character in raw password`() {
        assertFailsWith<IllegalArgumentException> {
            RawPassword("InvalidChar1?")
        }.let {
            assertEquals(
                "비밀번호에 허용되지 않는 특수문자가 포함되어 있습니다. 허용 특수문자: ${RawPassword.ALLOWED_SPECIAL_CHARS}",
                it.message
            )
        }
    }
}
