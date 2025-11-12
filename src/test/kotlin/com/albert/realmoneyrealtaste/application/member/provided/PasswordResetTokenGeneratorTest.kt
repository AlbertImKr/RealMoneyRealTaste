package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PasswordResetTokenGeneratorTest(
    val passwordResetTokenGenerator: PasswordResetTokenGenerator,
    val passwordResetTokenRepository: PasswordResetTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `generate - success - creates new password reset token`() {
        val memberId = 1L

        val token = passwordResetTokenGenerator.generate(memberId)

        assertAll(
            { assertNotNull(token) },
            { assertEquals(memberId, token.memberId) },
            { assertNotNull(token.token) },
            { assertTrue(token.token.isNotEmpty()) },
            { assertNotNull(token.createdAt) },
            { assertNotNull(token.expiresAt) })
    }

    @Test
    fun `generate - success - saves token to repository`() {
        val memberId = 2L

        val token = passwordResetTokenGenerator.generate(memberId)

        val savedToken = passwordResetTokenRepository.findByMemberId(memberId)
            ?: throw InvalidPasswordResetTokenException("유효하지 않은 비밀번호 재설정 토큰입니다.")

        assertAll(
            { assertNotNull(savedToken) },
            { assertEquals(token.token, savedToken.token) },
            { assertEquals(token.memberId, savedToken.memberId) })
    }

    @Test
    fun `generate - success - generates unique tokens for different members`() {
        val memberId1 = 3L
        val memberId2 = 4L

        val token1 = passwordResetTokenGenerator.generate(memberId1)
        val token2 = passwordResetTokenGenerator.generate(memberId2)

        assertAll(
            { assertTrue(token1.token != token2.token) },
            { assertEquals(memberId1, token1.memberId) },
            { assertEquals(memberId2, token2.memberId) })
    }

    @Test
    fun `generate - success - token has valid expiration time`() {
        val memberId = 5L

        val token = passwordResetTokenGenerator.generate(memberId)

        assertAll(
            { assertNotNull(token.expiresAt) },
            { assertTrue(token.expiresAt.isAfter(token.createdAt)) },
            { assertTrue(!token.isExpired()) })
    }

    @Test
    fun `generate - success - generates valid UUID format token`() {
        val memberId = 6L

        val token = passwordResetTokenGenerator.generate(memberId)

        val uuidPattern = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assertTrue(uuidPattern.matches(token.token))
    }

    @Test
    fun `generate - success - multiple tokens for same member are unique`() {
        val memberId = 7L

        val token1 = passwordResetTokenGenerator.generate(memberId)
        passwordResetTokenRepository.delete(token1)

        val token2 = passwordResetTokenGenerator.generate(memberId)

        assertTrue(token1.token != token2.token)
    }

    @Test
    fun `generate - success - creates token with all required properties`() {
        val memberId = 8L

        val token = passwordResetTokenGenerator.generate(memberId)

        assertAll(
            { assertEquals(memberId, token.memberId) },
            { assertNotNull(token.token) },
            { assertTrue(token.token.isNotEmpty()) },
            { assertNotNull(token.createdAt) },
            { assertNotNull(token.expiresAt) },
            { assertTrue(token.expiresAt.isAfter(token.createdAt)) })
    }
}
