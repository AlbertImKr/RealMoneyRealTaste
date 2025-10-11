package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.config.TestEmailSender
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.junit.jupiter.api.Assertions.assertAll
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberPasswordResetEmailSenderTest(
    val memberPasswordResetEmailSender: MemberPasswordResetEmailSender,
    emailSender: EmailSender,
    @param:Value("\${app.base-url}") val baseUrl: String,
    @param:Value("\${app.member.password-reset-token.expiration-hours}") val expirationHours: Long,
) : IntegrationTestBase() {

    var testEmailSender: TestEmailSender = emailSender as TestEmailSender

    @Test
    fun `sendResetEmail - success - sends email with correct recipient`() {
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertEquals(1, testEmailSender.count()) },
            { assertEquals(email, sentEmail.to) }
        )
    }

    @Test
    fun `sendResetEmail - success - sends email with correct subject`() {
        val email = Email("test@example.com")
        val nickname = Nickname("테스터")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail) },
            { assertEquals("[RealMoneyRealTaste] 비밀번호 재설정 안내", sentEmail.subject) }
        )
    }

    @Test
    fun `sendResetEmail - success - sends HTML email`() {
        val email = Email("html@example.com")
        val nickname = Nickname("HTML유저")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail) },
            { assertTrue(sentEmail.isHtml) }
        )
    }

    @Test
    fun `sendResetEmail - success - includes password reset link in content`() {
        val email = Email("link@example.com")
        val nickname = Nickname("링크테스트")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail.content) },
            { assertTrue(sentEmail.content.contains("$baseUrl/members/reset-password?token=")) }
        )
    }

    @Test
    fun `sendResetEmail - success - includes nickname in content`() {
        val email = Email("nickname@example.com")
        val nickname = Nickname("닉네임테스트")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail.content) },
            { assertTrue(sentEmail.content.contains(nickname.value)) }
        )
    }

    @Test
    fun `sendResetEmail - success - includes expiration hours in content`() {
        val email = Email("expiration@example.com")
        val nickname = Nickname("만료시간")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail.content) },
            { assertTrue(sentEmail.content.contains(expirationHours.toString())) }
        )
    }

    @Test
    fun `sendResetEmail - success - includes provided password reset token in content`() {
        val email = Email("token@example.com")
        val nickname = Nickname("토큰생성")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertNotNull(sentEmail.content) },
            { assertTrue(sentEmail.content.contains("token=${passwordResetToken.token}")) }
        )
    }

    @Test
    fun `sendResetEmail - success - uses different tokens for different password reset tokens`() {
        val email1 = Email("unique1@example.com")
        val email2 = Email("unique2@example.com")
        val nickname = Nickname("고유성테스트")
        val passwordResetToken1 = createTestPasswordResetToken()
        val passwordResetToken2 = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email1, nickname, passwordResetToken1)
        val firstEmail = testEmailSender.getLastSentEmail()!!

        memberPasswordResetEmailSender.sendResetEmail(email2, nickname, passwordResetToken2)
        val secondEmail = testEmailSender.getLastSentEmail()!!

        assertAll(
            { assertTrue(firstEmail.content != secondEmail.content) },
            { assertTrue(firstEmail.content.contains(passwordResetToken1.token)) },
            { assertTrue(secondEmail.content.contains(passwordResetToken2.token)) }
        )
    }

    @Test
    fun `sendResetEmail - success - sends email with complete password reset information`() {
        val email = Email("complete@example.com")
        val nickname = Nickname("완전한정보")
        val passwordResetToken = createTestPasswordResetToken()

        testEmailSender.clear()
        memberPasswordResetEmailSender.sendResetEmail(email, nickname, passwordResetToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertAll(
            { assertEquals(email, sentEmail.to) },
            { assertEquals("[RealMoneyRealTaste] 비밀번호 재설정 안내", sentEmail.subject) },
            { assertTrue(sentEmail.isHtml) },
            { assertTrue(sentEmail.content.contains(nickname.value)) },
            { assertTrue(sentEmail.content.contains(expirationHours.toString())) },
            { assertTrue(sentEmail.content.contains("token=${passwordResetToken.token}")) }
        )
    }

    private fun createTestPasswordResetToken(): PasswordResetToken {
        val now = LocalDateTime.now()
        return PasswordResetToken(
            memberId = 1L,
            token = UUID.randomUUID().toString(),
            createdAt = now,
            expiresAt = now.plusHours(expirationHours)
        )
    }
}
