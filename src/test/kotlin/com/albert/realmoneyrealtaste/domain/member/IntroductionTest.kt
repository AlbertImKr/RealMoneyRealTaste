package com.albert.realmoneyrealtaste.domain.member

import kotlin.test.Test
import kotlin.test.assertEquals

class IntroductionTest {

    @Test
    fun `test valid introduction`() {
        val intro = Introduction("This is a valid introduction.")

        assertEquals(intro, intro)
    }

    @Test
    fun `test empty introduction`() {
        val intro = Introduction("")

        assertEquals(intro, intro)
    }

    @Test
    fun `test maximum length introduction`() {
        val maxLengthIntro = "A".repeat(500)
        val intro = Introduction(maxLengthIntro)

        assertEquals(intro, intro)
    }

    @Test
    fun `test too long introduction`() {
        val tooLongIntro = "A".repeat(501)

        try {
            Introduction(tooLongIntro)
        } catch (e: IllegalArgumentException) {
            assertEquals("소개는 최대 500자 이내여야 합니다", e.message)
        }
    }
}
