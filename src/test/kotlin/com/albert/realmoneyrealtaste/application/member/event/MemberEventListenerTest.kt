package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.config.TestEmailSender
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import org.springframework.beans.factory.annotation.Value
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberEventListenerTest(
    val memberEventListener: MemberEventListener,
    @param:Value("\${app.base-url}") val baseUrl: String,
    emailSender: EmailSender,
) : IntegrationTestBase() {

    var testEmailSender: TestEmailSender = emailSender as TestEmailSender

    @Test
    fun `handleMemberRegistered - success - sends activation email`() {
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
    }

    @Test
    fun `handleResendActivationEmail - success - sends activation email`() {
        val memberId = 2L
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val event = ResendActivationEmailEvent(
            memberId = memberId,
            email = email,
            nickname = nickname
        )

        testEmailSender.clear()
        memberEventListener.handleResendActivationEmail(event)

        assertEquals(1, testEmailSender.count())
    }
}
