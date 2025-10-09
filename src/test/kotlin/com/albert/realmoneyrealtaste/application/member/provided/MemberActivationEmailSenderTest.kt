package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.config.TestEmailSender
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.Nickname
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
        val memberId = 1L
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        assertEquals(1, testEmailSender.count())
        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertEquals(email, sentEmail.to)
    }

    @Test
    fun `sendActivationEmail - success - sends email with correct subject`() {
        val memberId = 2L
        val email = Email("test@example.com")
        val nickname = Nickname("테스터")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertEquals("[RealMoneyRealTaste] 이메일 인증 안내", sentEmail.subject)
    }

    @Test
    fun `sendActivationEmail - success - sends HTML email`() {
        val memberId = 3L
        val email = Email("html@example.com")
        val nickname = Nickname("HTML유저")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.isHtml)
    }

    @Test
    fun `sendActivationEmail - success - includes activation link in content`() {
        val memberId = 4L
        val email = Email("link@example.com")
        val nickname = Nickname("링크테스트")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains("$baseUrl/members/activate?token="))
    }

    @Test
    fun `sendActivationEmail - success - includes nickname in content`() {
        val memberId = 5L
        val email = Email("nickname@example.com")
        val nickname = Nickname("닉네임테스트")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains(nickname.value))
    }

    @Test
    fun `sendActivationEmail - success - includes expiration hours in content`() {
        val memberId = 6L
        val email = Email("expiration@example.com")
        val nickname = Nickname("만료시간")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.content.contains(expirationHours.toString()))
    }

    @Test
    fun `sendActivationEmail - success - generates activation token for member`() {
        val memberId = 7L
        val email = Email("token@example.com")
        val nickname = Nickname("토큰생성")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId, email, nickname)

        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertNotNull(sentEmail.content)

        val tokenPattern = Regex("token=[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
        assertTrue(tokenPattern.containsMatchIn(sentEmail.content))
    }

    @Test
    fun `sendActivationEmail - success - generates unique tokens for different members`() {
        val memberId1 = 8L
        val memberId2 = 9L
        val email1 = Email("unique1@example.com")
        val email2 = Email("unique2@example.com")
        val nickname = Nickname("고유성테스트")

        testEmailSender.clear()
        memberActivationEmailSender.sendActivationEmail(memberId1, email1, nickname)
        val firstEmail = testEmailSender.getLastSentEmail()!!

        memberActivationEmailSender.sendActivationEmail(memberId2, email2, nickname)
        val secondEmail = testEmailSender.getLastSentEmail()!!

        assertTrue(firstEmail.content != secondEmail.content)

        val tokenPattern = Regex("token=([a-f0-9-]+)")
        val firstToken = tokenPattern.find(firstEmail.content)?.groupValues?.get(1)
        val secondToken = tokenPattern.find(secondEmail.content)?.groupValues?.get(1)

        assertNotNull(firstToken)
        assertNotNull(secondToken)
        assertTrue(firstToken != secondToken)
    }
}
