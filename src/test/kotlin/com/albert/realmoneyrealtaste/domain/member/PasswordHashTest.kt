package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.MemberFixture.Companion.TEST_ENCODER
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PasswordHashTest {

    @Test
    fun `of - success - creates password hash and matches raw password`() {
        val rawPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = TEST_ENCODER

        val passwordHash = PasswordHash.of(rawPassword, encoder)

        assertTrue(passwordHash.matches(rawPassword, encoder))
    }

    @Test
    fun `toString - success - does not expose the actual hash value`() {
        val rawPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = TEST_ENCODER

        val passwordHash = PasswordHash.of(rawPassword, encoder)

        assertEquals("비밀번호 해시(보안상 출력 불가)", passwordHash.toString())
    }

    @Test
    fun `of - failure - throws exception when hash value is blank`() {
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
