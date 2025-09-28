package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.MemberFixture.Companion.TEST_ENCODER
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PasswordHashTest {

    @Test
    fun `creates password hash`() {
        val rawPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = TEST_ENCODER

        val passwordHash = PasswordHash.of(rawPassword, encoder)

        assertEquals(true, passwordHash.matches(rawPassword, encoder))
    }

    @Test
    fun `toString does not expose the actual hash value`() {
        val rawPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = TEST_ENCODER

        val passwordHash = PasswordHash.of(rawPassword, encoder)

        assertEquals("비밀번호 해시(보안상 출력 불가)", passwordHash.toString())
    }

    @Test
    fun `hash value blank`() {
        val rawPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = mock(PasswordEncoder::class.java)
        `when`(encoder.encode(rawPassword)).thenReturn("")

        assertFailsWith<IllegalArgumentException> {
            PasswordHash.of(rawPassword, encoder)
        }.let {
            assertEquals("비밀번호 해시는 필수입니다", it.message)
        }
    }
}
