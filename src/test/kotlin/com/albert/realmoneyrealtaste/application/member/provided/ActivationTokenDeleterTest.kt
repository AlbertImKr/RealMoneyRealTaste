package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ActivationTokenDeleterTest(
    val activationTokenDeleter: ActivationTokenDeleter,
    val activationTokenGenerator: ActivationTokenGenerator,
    val activationTokenRepository: ActivationTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `delete - success - deletes existing activation token`() {
        val memberId = 1L
        val token = activationTokenGenerator.generate(memberId)

        val savedToken = activationTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)

        activationTokenDeleter.delete(token)

        val deletedToken = activationTokenRepository.findByMemberId(memberId)
        assertNull(deletedToken)
    }

    @Test
    fun `delete - success - removes token from repository`() {
        val memberId = 2L
        val token = activationTokenGenerator.generate(memberId)

        assertNotNull(activationTokenRepository.findByMemberId(memberId))

        activationTokenDeleter.delete(token)

        assertNull(activationTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - deletes specific token without affecting others`() {
        val memberId1 = 3L
        val memberId2 = 4L
        val token1 = activationTokenGenerator.generate(memberId1)
        activationTokenGenerator.generate(memberId2)

        assertNotNull(activationTokenRepository.findByMemberId(memberId1))
        assertNotNull(activationTokenRepository.findByMemberId(memberId2))

        activationTokenDeleter.delete(token1)

        assertNull(activationTokenRepository.findByMemberId(memberId1))
        assertNotNull(activationTokenRepository.findByMemberId(memberId2))
    }

    @Test
    fun `delete - success - handles deletion of already deleted token gracefully`() {
        val memberId = 5L
        val token = activationTokenGenerator.generate(memberId)

        activationTokenDeleter.delete(token)

        activationTokenDeleter.delete(token)

        assertNull(activationTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - deletes token after member activation`() {
        val memberId = 6L
        val token = activationTokenGenerator.generate(memberId)

        val savedToken = activationTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)

        activationTokenDeleter.delete(token)

        assertNull(activationTokenRepository.findByMemberId(memberId))
    }

    @Test
    fun `delete - success - allows new token generation after deletion`() {
        val memberId = 7L
        val firstToken = activationTokenGenerator.generate(memberId)

        activationTokenDeleter.delete(firstToken)

        activationTokenGenerator.generate(memberId)

        val savedToken = activationTokenRepository.findByMemberId(memberId)
        assertNotNull(savedToken)
    }
}
