package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PasswordResetTokenDeleterTest(
    val passwordResetTokenDeleter: PasswordResetTokenDeleter,
    val passwordResetTokenGenerator: PasswordResetTokenGenerator,
    val passwordResetTokenRepository: PasswordResetTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `delete - success - deletes existing password reset token`() {
        val memberId = 1L
        val token = passwordResetTokenGenerator.generate(memberId)

        val savedToken = passwordResetTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)

        passwordResetTokenDeleter.delete(token)

        val deletedToken = passwordResetTokenRepository.findByMemberId(memberId)
        assertNull(deletedToken)
    }

    @Test
    fun `delete - success - removes token from repository`() {
        val memberId = 2L
        val token = passwordResetTokenGenerator.generate(memberId)

        assertNotNull(passwordResetTokenRepository.findByMemberId(memberId))

        passwordResetTokenDeleter.delete(token)

        assertNull(passwordResetTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - deletes specific token without affecting others`() {
        val memberId1 = 3L
        val memberId2 = 4L
        val token1 = passwordResetTokenGenerator.generate(memberId1)
        passwordResetTokenGenerator.generate(memberId2)

        assertNotNull(passwordResetTokenRepository.findByMemberId(memberId1))
        assertNotNull(passwordResetTokenRepository.findByMemberId(memberId2))

        passwordResetTokenDeleter.delete(token1)

        assertNull(passwordResetTokenRepository.findByMemberId(memberId1))
        assertNotNull(passwordResetTokenRepository.findByMemberId(memberId2))
    }

    @Test
    fun `delete - success - handles deletion of already deleted token gracefully`() {
        val memberId = 5L
        val token = passwordResetTokenGenerator.generate(memberId)

        passwordResetTokenDeleter.delete(token)

        // 다시 삭제 시도
        passwordResetTokenDeleter.delete(token)

        assertNull(passwordResetTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - deletes token after password reset`() {
        val memberId = 6L
        val token = passwordResetTokenGenerator.generate(memberId)

        val savedToken = passwordResetTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)

        passwordResetTokenDeleter.delete(token)

        assertNull(passwordResetTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - allows new token generation after deletion`() {
        val memberId = 7L
        val firstToken = passwordResetTokenGenerator.generate(memberId)

        passwordResetTokenDeleter.delete(firstToken)

        passwordResetTokenGenerator.generate(memberId)

        val savedToken = passwordResetTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)
    }

    @Test
    fun `delete - success - deletes expired token`() {
        val memberId = 8L
        val token = passwordResetTokenGenerator.generate(memberId)

        assertNotNull(passwordResetTokenRepository.findByMemberId(memberId))

        passwordResetTokenDeleter.delete(token)

        assertNull(passwordResetTokenRepository.findByMemberId(memberId))
    }
}
