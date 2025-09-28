package com.albert.realmoneyrealtaste.domain.member

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PasswordTest {

    @Test
    fun `creates password`() {
        val password = Password("hashedPassword123")

        assertEquals("hashedPassword123", password.hash)
    }

    @Test
    fun `throws exception for blank password`() {
        assertFailsWith<IllegalArgumentException> {
            Password("")
        }.let {
            assertEquals("비밀번호 해시는 필수입니다", it.message)
        }
    }
}
