package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.event.PasswordResetRequestedEvent
import com.albert.realmoneyrealtaste.application.member.exception.PassWordResetException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordResetTokenNotFoundException
import com.albert.realmoneyrealtaste.application.member.exception.SendPasswordResetEmailException
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class PasswordResetterTest(
    val passwordResetter: PasswordResetter,
    val memberReader: MemberReader,
    val memberRepository: MemberRepository,
    val passwordResetTokenReader: PasswordResetTokenReader,
    val passwordResetTokenRepository: PasswordResetTokenRepository,
    val passwordEncoder: PasswordEncoder,
) : IntegrationTestBase() {

    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `sendPasswordResetEmail - success - generates token and publishes event`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val savedToken = passwordResetTokenReader.findByMemberId(member.requireId())
        assertNotNull(savedToken)
    }

    @Test
    fun `sendPasswordResetEmail - failure - does nothing when email does not exist`() {
        val nonExistentEmail = Email("notExists@gmail.com")
        applicationEvents.clear()

        assertFailsWith<SendPasswordResetEmailException> {
            passwordResetter.sendPasswordResetEmail(nonExistentEmail)
        }

        assertEquals(applicationEvents.stream(PasswordResetRequestedEvent::class.java).count().toInt(), 0)
    }

    @Test
    fun `sendPasswordResetEmail - success - deletes existing token before generating new one`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val firstToken = passwordResetTokenReader.findByMemberId(member.requireId())
        assertNotNull(firstToken)

        passwordResetter.sendPasswordResetEmail(member.email)

        val secondToken = passwordResetTokenReader.findByMemberId(member.requireId())
        assertAll(
            { assertNotNull(secondToken) },
            { assertTrue(firstToken.token != secondToken.token) }
        )
    }

    @Test
    fun `sendPasswordResetEmail - success - creates valid token`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val savedToken = passwordResetTokenReader.findByMemberId(member.requireId())
        assertAll(
            { assertNotNull(savedToken) },
            { assertEquals(member.requireId(), savedToken.memberId) },
            { assertNotNull(savedToken.token) },
            { assertTrue(savedToken.token.isNotEmpty()) },
            { assertNotNull(savedToken.createdAt) },
            { assertNotNull(savedToken.expiresAt) },
            { assertTrue(savedToken.expiresAt.isAfter(savedToken.createdAt)) }
        )
    }

    @Test
    fun `resetPassword - success - changes password and deletes token`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val token = passwordResetTokenReader.findByMemberId(member.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val updatedMember = memberReader.readMemberById(member.requireId())
        assertAll(
            { assertTrue(updatedMember.verifyPassword(newPassword, passwordEncoder)) },
            { assertFailsWith<PasswordResetTokenNotFoundException> { passwordResetTokenReader.findByMemberId(member.requireId()) } }
        )
    }

    @Test
    fun `resetPassword - failure - throws exception when token does not exist`() {
        val nonExistentToken = "non-existent-token"
        val newPassword = RawPassword("newPassword123!")

        assertFailsWith<PassWordResetException> {
            passwordResetter.resetPassword(nonExistentToken, newPassword)
        }
    }

    @Test
    fun `resetPassword - failure - throws exception when token is expired`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        val expiredToken = PasswordResetToken(
            memberId = member.requireId(),
            token = "expired-token",
            createdAt = LocalDateTime.now().minusHours(2),
            expiresAt = LocalDateTime.now().minusHours(1)
        )
        passwordResetTokenRepository.save(expiredToken)

        val newPassword = RawPassword("newPassword123!")

        assertFailsWith<PassWordResetException> {
            passwordResetter.resetPassword(expiredToken.token, newPassword)
        }

        assertFailsWith<PassWordResetException> {
            passwordResetter.resetPassword(expiredToken.token, newPassword)
        }
    }

    @Test
    fun `resetPassword - success - old password no longer works after reset`() {
        val member = MemberFixture.createMember()
        member.activate()
        val oldPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val token = passwordResetTokenReader.findByMemberId(member.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val updatedMember = memberReader.readMemberById(member.requireId())
        assertAll(
            { assertTrue(updatedMember.verifyPassword(newPassword, passwordEncoder)) },
            { assertTrue(!updatedMember.verifyPassword(oldPassword, passwordEncoder)) }
        )
    }

    @Test
    fun `resetPassword - failure - cannot reuse token after successful reset`() {
        val member = MemberFixture.createMember()
        member.activate()
        memberRepository.save(member)

        passwordResetter.sendPasswordResetEmail(member.email)

        val token = passwordResetTokenReader.findByMemberId(member.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val anotherPassword = RawPassword("anotherPassword123!")

        assertFailsWith<PassWordResetException> {
            passwordResetter.resetPassword(token.token, anotherPassword)
        }
    }

    @Test
    fun `sendPasswordResetEmail - success - multiple members can have tokens simultaneously`() {
        val member1 = MemberFixture.createMember(email = Email("member1@example.com"))
        val member2 = MemberFixture.createMember(email = Email("member2@example.com"))
        member1.activate()
        member2.activate()
        memberRepository.save(member1)
        memberRepository.save(member2)

        passwordResetter.sendPasswordResetEmail(member1.email)
        passwordResetter.sendPasswordResetEmail(member2.email)

        val token1 = passwordResetTokenReader.findByMemberId(member1.requireId())
        val token2 = passwordResetTokenReader.findByMemberId(member2.requireId())

        assertAll(
            { assertNotNull(token1) },
            { assertNotNull(token2) },
            { assertTrue(token1.token != token2.token) },
            { assertEquals(member1.requireId(), token1.memberId) },
            { assertEquals(member2.requireId(), token2.memberId) }
        )
    }

    @Test
    fun `resetPassword - success - only affects specific member password`() {
        val password1 = PasswordHash.of(MemberFixture.DEFAULT_RAW_PASSWORD, passwordEncoder)
        val password2 = PasswordHash.of(MemberFixture.DEFAULT_RAW_PASSWORD, passwordEncoder)
        val member1 = MemberFixture.createMember(email = Email("member1@example.com"), password = password1)
        val member2 = MemberFixture.createMember(email = Email("member2@example.com"), password = password2)
        member1.activate()
        member2.activate()
        memberRepository.save(member1)
        memberRepository.save(member2)

        passwordResetter.sendPasswordResetEmail(member1.email)

        val token1 = passwordResetTokenReader.findByMemberId(member1.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token1.token, newPassword)

        val updatedMember1 = memberReader.readMemberById(member1.requireId())
        val updatedMember2 = memberReader.readMemberById(member2.requireId())

        assertAll(
            { assertTrue(updatedMember1.verifyPassword(newPassword, passwordEncoder)) },
            { assertTrue(updatedMember2.verifyPassword(MemberFixture.DEFAULT_RAW_PASSWORD, passwordEncoder)) }
        )
    }
}
