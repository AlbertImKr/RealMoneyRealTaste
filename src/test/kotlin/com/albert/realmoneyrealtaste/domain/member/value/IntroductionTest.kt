package com.albert.realmoneyrealtaste.domain.member.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IntroductionTest {

    @Test
    fun `constructor - success - creates introduction with valid text`() {
        val validText = "This is a valid introduction."

        val intro = Introduction(validText)

        assertEquals(validText, intro.value)
    }

    @Test
    fun `constructor - success - creates introduction with empty text`() {
        val emptyText = ""

        val intro = Introduction(emptyText)

        assertEquals(emptyText, intro.value)
    }

    @Test
    fun `constructor - success - creates introduction with maximum length text`() {
        val maxLengthText = "A".repeat(500)

        val intro = Introduction(maxLengthText)

        assertEquals(maxLengthText, intro.value)
    }

    @Test
    fun `constructor - failure - throws exception when text exceeds maximum length`() {
        val tooLongText = "A".repeat(501)

        assertFailsWith<IllegalArgumentException> {
            Introduction(tooLongText)
        }.let {
            assertEquals("자기소개는 최대 500 자까지 작성할 수 있습니다", it.message)
        }
    }

    @Test
    fun `constructor - success - creates introduction with default empty value when no parameter`() {
        val intro = Introduction()

        assertEquals("", intro.value)
    }
}
