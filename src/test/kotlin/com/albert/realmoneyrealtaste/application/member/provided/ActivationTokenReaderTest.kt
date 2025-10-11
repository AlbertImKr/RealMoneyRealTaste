package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import org.junit.jupiter.api.Assertions.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ActivationTokenReaderTest(
    val activationTokenReader: ActivationTokenReader,
    val activationTokenGenerator: ActivationTokenGenerator,
    val activationTokenRepository: ActivationTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `findByToken - success - returns token when valid token exists`() {
        val memberId = 1L
        val generatedToken = activationTokenGenerator.generate(memberId)

        val foundToken = activationTokenReader.findByToken(generatedToken.token)

        assertAll(
            { assertNotNull(foundToken) },
            { assertEquals(generatedToken.token, foundToken.token) },
            { assertEquals(memberId, foundToken.memberId) }
        )
    }

    @Test
    fun `findByToken - success - returns correct token with all properties`() {
        val memberId = 2L
        val generatedToken = activationTokenGenerator.generate(memberId)

        val foundToken = activationTokenReader.findByToken(generatedToken.token)

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

        assertFailsWith<InvalidActivationTokenException> {
            activationTokenReader.findByToken(nonExistentToken)
        }
    }

    @Test
    fun `findByToken - failure - throws exception when token is empty string`() {
        assertFailsWith<InvalidActivationTokenException> {
            activationTokenReader.findByToken("")
        }
    }

    @Test
    fun `findByToken - success - finds token among multiple tokens`() {
        val memberId1 = 3L
        val memberId2 = 4L
        val token1 = activationTokenGenerator.generate(memberId1)
        val token2 = activationTokenGenerator.generate(memberId2)

        val foundToken1 = activationTokenReader.findByToken(token1.token)
        val foundToken2 = activationTokenReader.findByToken(token2.token)

        assertAll(
            { assertNotNull(foundToken1) },
            { assertNotNull(foundToken2) },
            { assertEquals(token1.token, foundToken1.token) },
            { assertEquals(token2.token, foundToken2.token) },
            { assertEquals(memberId1, foundToken1.memberId) },
            { assertEquals(memberId2, foundToken2.memberId) },
        )
    }

    @Test
    fun `findByToken - failure - throws exception after token is deleted`() {
        val memberId = 5L
        val generatedToken = activationTokenGenerator.generate(memberId)
        val tokenString = generatedToken.token

        activationTokenRepository.delete(generatedToken)

        assertFailsWith<InvalidActivationTokenException> {
            activationTokenReader.findByToken(tokenString)
        }
    }

    @Test
    fun `findByToken - success - reads token without modifying it`() {
        val memberId = 6L
        val generatedToken = activationTokenGenerator.generate(memberId)

        val foundToken1 = activationTokenReader.findByToken(generatedToken.token)
        val foundToken2 = activationTokenReader.findByToken(generatedToken.token)

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
    fun `findByToken - failure - throws exception with invalid UUID format token`() {
        val invalidToken = "invalid-token-format"

        assertFailsWith<InvalidActivationTokenException> {
            activationTokenReader.findByToken(invalidToken)
        }
    }
}
