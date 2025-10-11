package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.InvalidPasswordResetTokenException
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PasswordResetTokenReaderTest(
    val passwordResetTokenReader: PasswordResetTokenReader,
    val passwordResetTokenGenerator: PasswordResetTokenGenerator,
    val passwordResetTokenRepository: PasswordResetTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `findByToken - success - returns token when valid token exists`() {
        val memberId = 1L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        val foundToken = passwordResetTokenReader.findByToken(generatedToken.token)

        assertAll(
            { assertNotNull(foundToken) },
            { assertEquals(generatedToken.token, foundToken.token) },
            { assertEquals(memberId, foundToken.memberId) }
        )
    }

    @Test
    fun `findByToken - success - returns correct token with all properties`() {
        val memberId = 2L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        val foundToken = passwordResetTokenReader.findByToken(generatedToken.token)

        assertAll(
            { assertNotNull(foundToken) },
            { assertEquals(generatedToken.token, foundToken.token) },
            { assertEquals(generatedToken.memberId, foundToken.memberId) },
            { assertEquals(generatedToken.createdAt, foundToken.createdAt) },
            { assertEquals(generatedToken.expiresAt, foundToken.expiresAt) }
        )
    }

    @Test
    fun `findByToken - failure - throws exception when token does not exist`() {
        val nonExistentToken = "non-existent-token-12345"

        assertFailsWith<InvalidPasswordResetTokenException> {
            passwordResetTokenReader.findByToken(nonExistentToken)
        }
    }

    @Test
    fun `findByToken - failure - throws exception when token is empty string`() {
        assertFailsWith<InvalidPasswordResetTokenException> {
            passwordResetTokenReader.findByToken("")
        }
    }

    @Test
    fun `findByToken - success - finds token among multiple tokens`() {
        val memberId1 = 3L
        val memberId2 = 4L
        val token1 = passwordResetTokenGenerator.generate(memberId1)
        val token2 = passwordResetTokenGenerator.generate(memberId2)

        val foundToken1 = passwordResetTokenReader.findByToken(token1.token)
        val foundToken2 = passwordResetTokenReader.findByToken(token2.token)

        assertAll(
            { assertNotNull(foundToken1) },
            { assertNotNull(foundToken2) },
            { assertEquals(token1.token, foundToken1.token) },
            { assertEquals(token2.token, foundToken2.token) },
            { assertEquals(memberId1, foundToken1.memberId) },
            { assertEquals(memberId2, foundToken2.memberId) }
        )
    }

    @Test
    fun `findByToken - failure - throws exception after token is deleted`() {
        val memberId = 5L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)
        val tokenString = generatedToken.token

        passwordResetTokenRepository.delete(generatedToken)

        assertFailsWith<InvalidPasswordResetTokenException> {
            passwordResetTokenReader.findByToken(tokenString)
        }
    }

    @Test
    fun `findByToken - success - reads token without modifying it`() {
        val memberId = 6L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        val foundToken1 = passwordResetTokenReader.findByToken(generatedToken.token)
        val foundToken2 = passwordResetTokenReader.findByToken(generatedToken.token)

        assertAll(
            { assertNotNull(foundToken1) },
            { assertNotNull(foundToken2) },
            { assertEquals(foundToken1.token, foundToken2.token) },
            { assertEquals(foundToken1.memberId, foundToken2.memberId) },
            { assertEquals(foundToken1.createdAt, foundToken2.createdAt) },
            { assertEquals(foundToken1.expiresAt, foundToken2.expiresAt) }
        )
    }

    @Test
    fun `findByMemberId - success - returns token when valid member id exists`() {
        val memberId = 7L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        val foundToken = passwordResetTokenReader.findByMemberId(memberId)

        assertAll(
            { assertNotNull(foundToken) },
            { assertEquals(generatedToken.token, foundToken.token) },
            { assertEquals(memberId, foundToken.memberId) }
        )
    }

    @Test
    fun `findByMemberId - success - returns correct token with all properties`() {
        val memberId = 8L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        val foundToken = passwordResetTokenReader.findByMemberId(memberId)

        assertAll(
            { assertNotNull(foundToken) },
            { assertEquals(generatedToken.token, foundToken.token) },
            { assertEquals(generatedToken.memberId, foundToken.memberId) },
            { assertEquals(generatedToken.createdAt, foundToken.createdAt) },
            { assertEquals(generatedToken.expiresAt, foundToken.expiresAt) }
        )
    }

    @Test
    fun `findByMemberId - failure - throws exception when member has no token`() {
        val nonExistentMemberId = 999L

        assertFailsWith<InvalidPasswordResetTokenException> {
            passwordResetTokenReader.findByMemberId(nonExistentMemberId)
        }
    }

    @Test
    fun `findByMemberId - success - finds correct token for specific member`() {
        val memberId1 = 9L
        val memberId2 = 10L
        val token1 = passwordResetTokenGenerator.generate(memberId1)
        val token2 = passwordResetTokenGenerator.generate(memberId2)

        val foundToken1 = passwordResetTokenReader.findByMemberId(memberId1)
        val foundToken2 = passwordResetTokenReader.findByMemberId(memberId2)

        assertAll(
            { assertEquals(token1.token, foundToken1.token) },
            { assertEquals(token2.token, foundToken2.token) },
            { assertEquals(memberId1, foundToken1.memberId) },
            { assertEquals(memberId2, foundToken2.memberId) }
        )
    }

    @Test
    fun `findByMemberId - failure - throws exception after token is deleted`() {
        val memberId = 11L
        val generatedToken = passwordResetTokenGenerator.generate(memberId)

        passwordResetTokenRepository.delete(generatedToken)

        assertFailsWith<InvalidPasswordResetTokenException> {
            passwordResetTokenReader.findByMemberId(memberId)
        }
    }

    @Test
    fun `findByMemberId - success - reads token without modifying it`() {
        val memberId = 12L
        passwordResetTokenGenerator.generate(memberId)

        val foundToken1 = passwordResetTokenReader.findByMemberId(memberId)
        val foundToken2 = passwordResetTokenReader.findByMemberId(memberId)

        assertAll(
            { assertNotNull(foundToken1) },
            { assertNotNull(foundToken2) },
            { assertEquals(foundToken1.token, foundToken2.token) },
            { assertEquals(foundToken1.memberId, foundToken2.memberId) },
            { assertEquals(foundToken1.createdAt, foundToken2.createdAt) },
            { assertEquals(foundToken1.expiresAt, foundToken2.expiresAt) }
        )
    }
}
