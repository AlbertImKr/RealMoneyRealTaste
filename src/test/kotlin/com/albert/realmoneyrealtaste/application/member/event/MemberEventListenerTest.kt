package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.config.TestEmailSender
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import org.springframework.beans.factory.annotation.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberEventListenerTest(
    val memberEventListener: MemberEventListener,
    @param:Value("\${app.base-url}") val baseUrl: String,
    emailSender: EmailSender,
) : IntegrationTestBase() {

    var testEmailSender: TestEmailSender = emailSender as TestEmailSender

    @Test
    fun `handleMemberRegistered - success - sends activation email with correct content`() {
        val memberId = 1L
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val event = MemberRegisteredEvent(
            memberId = memberId,
            email = email,
            nickname = nickname
        )

        testEmailSender.clear()
        memberEventListener.handleMemberRegistered(event)

        assertEquals(1, testEmailSender.count())
        val sentEmail = testEmailSender.getLastSentEmail()!!
        assertTrue(sentEmail.subject.contains("이메일 인증"))
        assertTrue(sentEmail.isHtml)
        assertTrue(sentEmail.content.contains("$baseUrl/members/activate?token="))
        assertTrue(sentEmail.content.contains(nickname.value))
    }
}
