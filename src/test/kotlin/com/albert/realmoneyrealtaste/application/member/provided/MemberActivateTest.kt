package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.util.MemberFixture
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemberActivateTest(
    val memberActivate: MemberActivate,
    val memberRegister: MemberRegister,
    val activationTokenGenerator: ActivationTokenGenerator,
    val activationTokenRepository: ActivationTokenRepository,
) : IntegrationTestBase() {

    @Test
    fun `activate - success - activates member and invalidates token`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val member = memberRegister.register(request)
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")

        val activatedMember = memberActivate.activate(token.token)

        assertEquals(MemberStatus.ACTIVE, activatedMember.status)
        assertFailsWith<MemberActivateException> {
            memberActivate.activate(token.token)
        }
    }

    @Test
    fun `activate - failure - throws exception when token is invalid`() {
        val invalidToken = "invalid.token.value"

        assertFailsWith<MemberActivateException> {
            memberActivate.activate(invalidToken)
        }
    }

    @Test
    fun `activate - failure - throws exception when token already used`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val member = memberRegister.register(request)
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")

        memberActivate.activate(token.token)

        assertFailsWith<MemberActivateException> {
            memberActivate.activate(token.token)
        }
    }

    @Test
    fun `activate - failure - throws exception when token is expired`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val member = memberRegister.register(request)

        activationTokenRepository.findByMemberId(member.id!!)
            ?.let { activationTokenRepository.delete(it) }
        flushAndClear()

        val expiredToken = activationTokenRepository.save(
            ActivationToken(
                member.id!!,
                UUID.randomUUID().toString(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
            )
        )

        assertFailsWith<MemberActivateException> {
            memberActivate.activate(expiredToken.token)
        }
    }

    @Test
    fun `activate - failure - throws exception when member does not exist`() {
        val nonExistentMemberId = 999999L
        val token = activationTokenGenerator.generate(nonExistentMemberId)

        assertFailsWith<MemberActivateException> {
            memberActivate.activate(token.token)
        }
    }

    @Test
    fun `activate - failure - throws exception when member is already active`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val member = memberRegister.register(request)
        member.activate()
        val token = activationTokenRepository.findByMemberId(member.requireId())
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")

        assertFailsWith<MemberActivateException> {
            memberActivate.activate(token.token)
        }
    }

    @Test
    fun `resendActivationEmail - success - publishes event for pending member`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        memberRegister.register(request)

        memberActivate.resendActivationEmail(email)
    }

    @Test
    fun `resendActivationEmail - failure - throws exception when member not found`() {
        val nonExistentEmail = Email("nonexistent@example.com")

        assertFailsWith<MemberResendActivationEmailException> {
            memberActivate.resendActivationEmail(nonExistentEmail)
        }
    }

    @Test
    fun `resendActivationEmail - failure - throws exception when member already active`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val member = memberRegister.register(request)
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("활성 토큰을 찾을 수 없습니다. 회원 ID: ${member.id}")

        memberActivate.activate(token.token)

        assertFailsWith<MemberResendActivationEmailException> {
            memberActivate.resendActivationEmail(email)
        }
    }
}
