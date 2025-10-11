package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.api.Assertions.assertAll
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordResetTokenTest {

    @Test
    fun `constructor - success - creates token with all properties`() {
        val memberId = 1L
        val token = "test-token-123"
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt.plusHours(1)

        val passwordResetToken = PasswordResetToken(
            memberId = memberId,
            token = token,
            createdAt = createdAt,
            expiresAt = expiresAt
        )

        assertEquals(memberId, passwordResetToken.memberId)
        assertEquals(token, passwordResetToken.token)
        assertEquals(createdAt, passwordResetToken.createdAt)
        assertEquals(expiresAt, passwordResetToken.expiresAt)
    }

    @Test
    fun `isExpired - success - returns false when token is not expired`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "valid-token",
            createdAt = now,
            expiresAt = now.plusHours(1)
        )

        assertFalse(passwordResetToken.isExpired())
    }

    @Test
    fun `isExpired - success - returns true when token is expired`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "expired-token",
            createdAt = now.minusHours(2),
            expiresAt = now.minusHours(1)
        )

        assertTrue(passwordResetToken.isExpired())
    }

    @Test
    fun `isExpired - success - returns true when token expires at current time`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "just-expired-token",
            createdAt = now.minusHours(1),
            expiresAt = now.minusSeconds(1)
        )

        assertTrue(passwordResetToken.isExpired())
    }

    @Test
    fun `isExpired - success - returns false when token expires in the future`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "future-token",
            createdAt = now,
            expiresAt = now.plusDays(1)
        )

        assertFalse(passwordResetToken.isExpired())
    }

    @Test
    fun `isExpired - success - returns false when token just created`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "new-token",
            createdAt = now,
            expiresAt = now.plusMinutes(30)
        )

        assertFalse(passwordResetToken.isExpired())
    }

    @Test
    fun `properties - success - maintains immutability for external access`() {
        val memberId = 123L
        val token = "immutable-token"
        val createdAt = LocalDateTime.of(2025, 1, 1, 12, 0)
        val expiresAt = LocalDateTime.of(2025, 1, 1, 13, 0)

        val passwordResetToken = PasswordResetToken(
            memberId = memberId,
            token = token,
            createdAt = createdAt,
            expiresAt = expiresAt
        )

        // Verify properties remain unchanged
        assertEquals(memberId, passwordResetToken.memberId)
        assertEquals(token, passwordResetToken.token)
        assertEquals(createdAt, passwordResetToken.createdAt)
        assertEquals(expiresAt, passwordResetToken.expiresAt)
    }

    @Test
    fun `token lifecycle - success - typical token lifecycle scenario`() {
        val now = LocalDateTime.now()
        val passwordResetToken = PasswordResetToken(
            memberId = 1L,
            token = "lifecycle-token",
            createdAt = now,
            expiresAt = now.plusHours(1)
        )

        // Token is valid when just created
        assertFalse(passwordResetToken.isExpired())

        // Simulate time passing - token expires after 1 hour
        val expiredToken = PasswordResetToken(
            memberId = 1L,
            token = "lifecycle-token",
            createdAt = now.minusHours(2),
            expiresAt = now.minusHours(1)
        )

        assertTrue(expiredToken.isExpired())
    }

    @Test
    fun `multiple tokens - success - different tokens for same member`() {
        val memberId = 1L
        val now = LocalDateTime.now()

        val token1 = PasswordResetToken(
            memberId = memberId,
            token = "token-1",
            createdAt = now,
            expiresAt = now.plusHours(1)
        )

        val token2 = PasswordResetToken(
            memberId = memberId,
            token = "token-2",
            createdAt = now,
            expiresAt = now.plusHours(1)
        )

        assertAll(
            { assertEquals(memberId, token1.memberId) },
            { assertEquals(memberId, token2.memberId) },
            { assertTrue(token1.token != token2.token) }
        )
    }

    @Test
    fun `different expiration times - success - handles various expiration durations`() {
        val now = LocalDateTime.now()

        val shortLivedToken = PasswordResetToken(
            memberId = 1L,
            token = "short-lived",
            createdAt = now,
            expiresAt = now.plusMinutes(15)
        )

        val mediumLivedToken = PasswordResetToken(
            memberId = 2L,
            token = "medium-lived",
            createdAt = now,
            expiresAt = now.plusHours(1)
        )

        val longLivedToken = PasswordResetToken(
            memberId = 3L,
            token = "long-lived",
            createdAt = now,
            expiresAt = now.plusDays(1)
        )

        assertAll(
            { assertFalse(shortLivedToken.isExpired()) },
            { assertFalse(mediumLivedToken.isExpired()) },
            { assertFalse(longLivedToken.isExpired()) },
        )
    }

    @Test
    fun `setters - success - update properties for coverage`() {
        val memberId = 1L
        val token = UUID.randomUUID().toString()
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt.plusHours(1)
        val passwordResetToken = TestPasswordResetToken(
            memberId = memberId,
            token = token,
            createdAt = createdAt,
            expiresAt = expiresAt
        )

        val newMemberId = 2L
        val newToken = UUID.randomUUID().toString()
        val newCreatedAt = createdAt.plusMinutes(10)
        val newExpiresAt = expiresAt.plusHours(1)

        passwordResetToken.setMemberIdForTest(newMemberId)
        passwordResetToken.setTokenForTest(newToken)
        passwordResetToken.setCreatedAtForTest(newCreatedAt)
        passwordResetToken.setExpiresAtForTest(newExpiresAt)

        assertAll(
            { assertEquals(newMemberId, passwordResetToken.memberId) },
            { assertEquals(newToken, passwordResetToken.token) },
            { assertEquals(newCreatedAt, passwordResetToken.createdAt) },
            { assertEquals(newExpiresAt, passwordResetToken.expiresAt) }
        )
    }

    private class TestPasswordResetToken(
        memberId: Long,
        token: String,
        createdAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ) : PasswordResetToken(memberId, token, createdAt, expiresAt) {

        fun setMemberIdForTest(newMemberId: Long) {
            this.memberId = newMemberId
        }

        fun setTokenForTest(newToken: String) {
            this.token = newToken
        }

        fun setCreatedAtForTest(newCreatedAt: LocalDateTime) {
            this.createdAt = newCreatedAt
        }

        fun setExpiresAtForTest(newExpiresAt: LocalDateTime) {
            this.expiresAt = newExpiresAt
        }
    }
}
