package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.api.Assertions
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivationTokenTest {

    @Test
    fun `expiration check works correctly`() {
        val now = LocalDateTime.now()
        val createdAt = now.minusHours(2)
        val expiresAt = now.minusHours(1)
        val token = "sample-token"
        val memberId = 1L
        val activationToken = ActivationToken(
            memberId = memberId,
            token = token,
            createdAt = createdAt,
            expiresAt = expiresAt
        )

        Assertions.assertAll(
            { assertTrue(activationToken.isExpired()) },
            { assertEquals(memberId, activationToken.memberId) },
            { assertEquals(token, activationToken.token) },
            { assertEquals(createdAt, activationToken.createdAt) },
            { assertEquals(expiresAt, activationToken.expiresAt) },
        )
    }

    @Test
    fun `non-expired token check works correctly`() {
        val now = LocalDateTime.now()
        val activationToken = ActivationToken(
            memberId = 1L,
            token = "sample-token",
            createdAt = now.minusHours(1),
            expiresAt = now.plusHours(1)
        )

        assertFalse(activationToken.isExpired())
    }

    @Test
    fun `test setters for code coverage`() {
        val now = LocalDateTime.now()
        val activationToken = TestActivationToken(
            memberId = 1L,
            token = "sample-token",
            createdAt = now.minusHours(1),
            expiresAt = now.plusHours(1),
        )
        val newCreatedAt = now.minusHours(2)
        val newExpiresAt = now.plusHours(2)

        activationToken.setMemberIdForTest(2L)
        activationToken.setTokenForTest("new-token")
        activationToken.setCreatedAtForTest(newCreatedAt)
        activationToken.setExpiresAtForTest(newExpiresAt)

        Assertions.assertAll(
            { assertEquals(2L, activationToken.memberId) },
            { assertEquals("new-token", activationToken.token) },
            { assertEquals(newCreatedAt, activationToken.createdAt) },
            { assertEquals(newExpiresAt, activationToken.expiresAt) },
        )
    }

    private class TestActivationToken(
        memberId: Long,
        token: String,
        createdAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ) : ActivationToken(memberId, token, createdAt, expiresAt) {

        fun setMemberIdForTest(memberId: Long) {
            this.memberId = memberId
        }

        fun setTokenForTest(token: String) {
            this.token = token
        }

        fun setCreatedAtForTest(createdAt: LocalDateTime) {
            this.createdAt = createdAt
        }

        fun setExpiresAtForTest(expiresAt: LocalDateTime) {
            this.expiresAt = expiresAt
        }
    }
}
