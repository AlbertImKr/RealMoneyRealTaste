package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ActivationTokenRepositoryTest(
    val activationTokenRepository: ActivationTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `save - success - saves and returns activation token`() {
        val token = createActivationToken(memberId = 1L)

        val savedToken = activationTokenRepository.save(token)

        assertNotNull(savedToken)
        assertEquals(token.memberId, savedToken.memberId)
        assertEquals(token.token, savedToken.token)
        assertEquals(token.createdAt, savedToken.createdAt)
        assertEquals(token.expiresAt, savedToken.expiresAt)
    }

    @Test
    fun `save - success - assigns id to new token`() {
        val token = createActivationToken(memberId = 2L)

        val savedToken = activationTokenRepository.save(token)

        assertNotNull(savedToken.id)
    }

    @Test
    fun `findByToken - success - returns token when exists`() {
        val token = createActivationToken(memberId = 3L)
        val savedToken = activationTokenRepository.save(token)
        flushAndClear()

        val foundToken = activationTokenRepository.findByToken(savedToken.token)

        assertNotNull(foundToken)
        assertEquals(savedToken.id, foundToken.id)
        assertEquals(savedToken.token, foundToken.token)
        assertEquals(savedToken.memberId, foundToken.memberId)
    }

    @Test
    fun `findByToken - failure - returns null when token does not exist`() {
        val nonExistentToken = "non-existent-token-${UUID.randomUUID()}"

        val foundToken = activationTokenRepository.findByToken(nonExistentToken)

        assertNull(foundToken)
    }

    @Test
    fun `findByMemberId - success - returns token when exists`() {
        val memberId = 4L
        val token = createActivationToken(memberId = memberId)
        val savedToken = activationTokenRepository.save(token)
        flushAndClear()

        val foundToken = activationTokenRepository.findByMemberId(memberId)

        assertNotNull(foundToken)
        assertEquals(savedToken.id, foundToken.id)
        assertEquals(savedToken.token, foundToken.token)
        assertEquals(memberId, foundToken.memberId)
    }

    @Test
    fun `findByMemberId - failure - returns null when token does not exist`() {
        val nonExistentMemberId = 999999L

        val foundToken = activationTokenRepository.findByMemberId(nonExistentMemberId)

        assertNull(foundToken)
    }

    @Test
    fun `delete - success - removes token from repository`() {
        val token = createActivationToken(memberId = 5L)
        val savedToken = activationTokenRepository.save(token)
        flushAndClear()

        activationTokenRepository.delete(savedToken)
        flushAndClear()

        val foundToken = activationTokenRepository.findByToken(savedToken.token)
        assertNull(foundToken)
    }

    @Test
    fun `delete - success - allows finding by memberId to return null after deletion`() {
        val memberId = 6L
        val token = createActivationToken(memberId = memberId)
        val savedToken = activationTokenRepository.save(token)
        flushAndClear()

        activationTokenRepository.delete(savedToken)
        flushAndClear()

        val foundToken = activationTokenRepository.findByMemberId(memberId)
        assertNull(foundToken)
    }

    @Test
    fun `findByToken - success - returns correct token when multiple tokens exist`() {
        val token1 = createActivationToken(memberId = 8L)
        val token2 = createActivationToken(memberId = 9L)
        val savedToken1 = activationTokenRepository.save(token1)
        activationTokenRepository.save(token2)
        flushAndClear()

        val foundToken = activationTokenRepository.findByToken(savedToken1.token)

        assertNotNull(foundToken)
        assertEquals(savedToken1.token, foundToken.token)
        assertEquals(savedToken1.memberId, foundToken.memberId)
    }

    @Test
    fun `findByMemberId - success - returns only token for specified member`() {
        val memberId1 = 10L
        val memberId2 = 11L
        val token1 = createActivationToken(memberId = memberId1)
        val token2 = createActivationToken(memberId = memberId2)
        activationTokenRepository.save(token1)
        activationTokenRepository.save(token2)
        flushAndClear()

        val foundToken = activationTokenRepository.findByMemberId(memberId1)

        assertNotNull(foundToken)
        assertEquals(memberId1, foundToken.memberId)
    }

    private fun createActivationToken(
        memberId: Long,
        expirationHours: Long = 24L,
    ): ActivationToken {
        val createdAt = LocalDateTime.now()
        return ActivationToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = createdAt,
            expiresAt = createdAt.plusHours(expirationHours)
        )
    }
}
