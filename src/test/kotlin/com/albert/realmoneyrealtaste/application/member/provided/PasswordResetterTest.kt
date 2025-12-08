package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.PassWordResetException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordResetTokenNotFoundException
import com.albert.realmoneyrealtaste.application.member.exception.SendPasswordResetEmailException
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
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
        val member = createMember()
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val savedToken = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        assertNotNull(savedToken)
    }

    @Test
    fun `sendPasswordResetEmail - failure - does nothing when email does not exist`() {
        val nonExistentEmail = Email("notExists@gmail.com")
        applicationEvents.clear()

        assertFailsWith<SendPasswordResetEmailException> {
            passwordResetter.sendPasswordResetEmail(nonExistentEmail.address)
        }

    }

    @Test
    fun `sendPasswordResetEmail - success - deletes existing token before generating new one`() {
        val member = createMember()
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val firstToken = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        assertNotNull(firstToken)

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val secondToken = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        assertAll(
            { assertNotNull(secondToken) },
            { assertTrue(firstToken.token != secondToken.token) }
        )
    }

    @Test
    fun `sendPasswordResetEmail - success - creates valid token`() {
        val member = createMember()
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val savedToken = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        assertAll(
            { assertNotNull(savedToken) },
            { assertEquals(savedMember.requireId(), savedToken.memberId) },
            { assertNotNull(savedToken.token) },
            { assertTrue(savedToken.token.isNotEmpty()) },
            { assertNotNull(savedToken.createdAt) },
            { assertNotNull(savedToken.expiresAt) },
            { assertTrue(savedToken.expiresAt.isAfter(savedToken.createdAt)) }
        )
    }

    @Test
    fun `resetPassword - success - changes password and deletes token`() {
        val member = createMember()
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val token = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val updatedMember = memberReader.readMemberById(savedMember.requireId())
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
        val member = createMember()
        memberRepository.save(member)
        member.activate()

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
        val member = createMember()
        val oldPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val token = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val updatedMember = memberReader.readMemberById(savedMember.requireId())
        assertAll(
            { assertTrue(updatedMember.verifyPassword(newPassword, passwordEncoder)) },
            { assertTrue(!updatedMember.verifyPassword(oldPassword, passwordEncoder)) }
        )
    }

    @Test
    fun `resetPassword - failure - cannot reuse token after successful reset`() {
        val member = createMember()
        val savedMember = memberRepository.save(member)
        savedMember.activate()

        passwordResetter.sendPasswordResetEmail(savedMember.email.address)

        val token = passwordResetTokenReader.findByMemberId(savedMember.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token.token, newPassword)

        val anotherPassword = RawPassword("anotherPassword123!")

        assertFailsWith<PassWordResetException> {
            passwordResetter.resetPassword(token.token, anotherPassword)
        }
    }

    @Test
    fun `sendPasswordResetEmail - success - multiple members can have tokens simultaneously`() {
        val member1 = createMember(email = Email("member1@example.com"))
        val member2 = createMember(email = Email("member2@example.com"))
        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)
        savedMember1.activate()
        savedMember2.activate()

        passwordResetter.sendPasswordResetEmail(savedMember1.email.address)
        passwordResetter.sendPasswordResetEmail(savedMember2.email.address)

        val token1 = passwordResetTokenReader.findByMemberId(savedMember1.requireId())
        val token2 = passwordResetTokenReader.findByMemberId(savedMember2.requireId())

        assertAll(
            { assertNotNull(token1) },
            { assertNotNull(token2) },
            { assertTrue(token1.token != token2.token) },
            { assertEquals(savedMember1.requireId(), token1.memberId) },
            { assertEquals(savedMember2.requireId(), token2.memberId) }
        )
    }

    @Test
    fun `resetPassword - success - only affects specific member password`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val member1 = createMember(email = Email("member1@example.com"), password = password)
        val member2 = createMember(email = Email("member2@example.com"), password = password)

        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)

        savedMember1.activate()
        savedMember2.activate()

        passwordResetter.sendPasswordResetEmail(savedMember1.email.address)

        val token1 = passwordResetTokenReader.findByMemberId(savedMember1.requireId())
        val newPassword = RawPassword("newPassword123!")

        passwordResetter.resetPassword(token1.token, newPassword)

        val updatedMember1 = memberReader.readMemberById(savedMember1.requireId())
        val updatedMember2 = memberReader.readMemberById(savedMember2.requireId())

        assertAll(
            { assertTrue(updatedMember1.verifyPassword(newPassword, passwordEncoder)) },
            { assertTrue(updatedMember2.verifyPassword(MemberFixture.DEFAULT_RAW_PASSWORD, passwordEncoder)) }
        )
    }

    fun createMember(
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
        password: RawPassword = MemberFixture.DEFAULT_RAW_PASSWORD,
    ): Member {
        return Member.register(
            email = email,
            nickname = nickname,
            password = PasswordHash.of(password, passwordEncoder)
        )
    }
}
