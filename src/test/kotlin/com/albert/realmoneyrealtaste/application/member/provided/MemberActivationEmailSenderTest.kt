package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.config.TestEmailSender
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.util.MemberFixture
import org.springframework.beans.factory.annotation.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberActivationEmailSenderTest(
    val memberActivationEmailSender: MemberActivationEmailSender,
    emailSender: EmailSender,
    @param:Value("\${app.base-url}") val baseUrl: String,
    @param:Value("\${app.member.activation-token.expiration-hours}") val expirationHours: Long,
) : IntegrationTestBase() {

    var testEmailSender: TestEmailSender = emailSender as TestEmailSender

    @Test
    fun `sendActivationEmail - success - sends email with correct recipient`() {
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        assertEquals(1, testEmailSender.count())
        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertEquals(email, sentEmail.to)
    }

    @Test
    fun `sendActivationEmail - success - sends email with correct subject`() {
        val email = Email("test@example.com")
        val nickname = Nickname("테스터")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertEquals("[RealMoneyRealTaste] 이메일 인증 안내", sentEmail.subject)
    }

    @Test
    fun `sendActivationEmail - success - sends HTML email`() {
        val email = Email("html@example.com")
        val nickname = Nickname("HTML유저")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.isHtml)
    }

    @Test
    fun `sendActivationEmail - success - includes activation link in content`() {
        val email = Email("link@example.com")
        val nickname = Nickname("링크테스트")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains("$baseUrl/members/activate?token="))
    }

    @Test
    fun `sendActivationEmail - success - includes nickname in content`() {
        val email = Email("nickname@example.com")
        val nickname = Nickname("닉네임테스트")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains(nickname.value))
    }

    @Test
    fun `sendActivationEmail - success - includes expiration hours in content`() {
        val email = Email("expiration@example.com")
        val nickname = Nickname("만료시간")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains(expirationHours.toString()))
    }

    @Test
    fun `sendActivationEmail - success - includes provided activation token in content`() {
        val email = Email("token@example.com")
        val nickname = Nickname("토큰생성")
        val activationToken = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email, nickname, activationToken)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertNotNull(sentEmail.content)
        assertTrue(sentEmail.content.contains("token=${activationToken.token}"))
    }

    @Test
    fun `sendActivationEmail - success - uses different tokens for different activation tokens`() {
        val email1 = Email("unique1@example.com")
        val email2 = Email("unique2@example.com")
        val nickname = Nickname("고유성테스트")
        val activationToken1 = createTestActivationToken()
        val activationToken2 = createTestActivationToken()

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(email1, nickname, activationToken1)
        val firstEmail = testEmailSender.getLastSentEmail()!!

        memberActivationEmailSender.sendActivationEmail(email2, nickname, activationToken2)
        val secondEmail = testEmailSender.getLastSentEmail()!!

        assertTrue(firstEmail.content != secondEmail.content)
        assertTrue(firstEmail.content.contains(activationToken1.token))
        assertTrue(secondEmail.content.contains(activationToken2.token))
    }

    private fun createTestActivationToken(memberId: Long = 1L): ActivationToken {
        return MemberFixture.createActivationToken(memberId)
    }
}
