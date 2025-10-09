package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActivationTokenGeneratorTest(
    val activationTokenGenerator: ActivationTokenGenerator,
    val activationTokenRepository: ActivationTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `generate - success - creates new activation token`() {
        val memberId = 1L
        val expirationHours = 24L

        val token = activationTokenGenerator.generate(memberId, expirationHours)

        assertNotNull(token)
        assertEquals(memberId, token.memberId)
        assertNotNull(token.token)
        assertTrue(token.token.isNotEmpty())
        assertNotNull(token.createdAt)
        assertNotNull(token.expiresAt)
    }

    @Test
    fun `generate - success - sets correct expiration time`() {
        val memberId = 2L
        val expirationHours = 48L
        val beforeGeneration = LocalDateTime.now()

        val token = activationTokenGenerator.generate(memberId, expirationHours)

        val afterGeneration = LocalDateTime.now()
        val expectedExpiresAt = beforeGeneration.plusHours(expirationHours)

        assertTrue(token.expiresAt.isAfter(expectedExpiresAt.minusSeconds(5)))
        assertTrue(token.expiresAt.isBefore(afterGeneration.plusHours(expirationHours).plusSeconds(5)))
    }

    @Test
    fun `generate - success - deletes existing token and creates new one`() {
        val memberId = 3L
        val expirationHours = 24L

        val firstToken = activationTokenGenerator.generate(memberId, expirationHours)
        flushAndClear()

        val secondToken = activationTokenGenerator.generate(memberId, expirationHours)

        assertNotNull(secondToken)
        assertEquals(memberId, secondToken.memberId)
        assertTrue(firstToken.token != secondToken.token)

        val existingTokens = activationTokenRepository.findByMemberId(memberId)
        assertNotNull(existingTokens)
        assertEquals(secondToken.token, existingTokens.token)
    }

    @Test
    fun `generate - success - saves token to repository`() {
        val memberId = 4L
        val expirationHours = 24L

        val token = activationTokenGenerator.generate(memberId, expirationHours)
        flushAndClear()

        val savedToken = activationTokenRepository.findByMemberId(memberId)

        assertNotNull(savedToken)
        assertEquals(token.token, savedToken.token)
        assertEquals(token.memberId, savedToken.memberId)
    }

    @Test
    fun `generate - success - generates unique tokens for different members`() {
        val memberId1 = 5L
        val memberId2 = 6L
        val expirationHours = 24L

        val token1 = activationTokenGenerator.generate(memberId1, expirationHours)
        val token2 = activationTokenGenerator.generate(memberId2, expirationHours)

        assertTrue(token1.token != token2.token)
        assertEquals(memberId1, token1.memberId)
        assertEquals(memberId2, token2.memberId)
    }

    @Test
    fun `generate - success - creates expired token with negative expiration hours`() {
        val memberId = 8L
        val expirationHours = -1L

        val token = activationTokenGenerator.generate(memberId, expirationHours)

        assertNotNull(token)
        assertTrue(token.isExpired())
        assertTrue(token.expiresAt.isBefore(LocalDateTime.now()))
    }
}
