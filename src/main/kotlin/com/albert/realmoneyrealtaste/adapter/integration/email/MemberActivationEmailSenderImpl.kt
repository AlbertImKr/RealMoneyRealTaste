package com.albert.realmoneyrealtaste.adapter.integration.email

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivationEmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailTemplate
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MemberActivationEmailSenderImpl(
    private val tokenGenerator: ActivationTokenGenerator,
    private val emailSender: EmailSender,
    private val emailTemplate: EmailTemplate,
    @param:Value("\${app.member.activation-token.expiration-hours}") private val expirationHours: Long,
    @param:Value("\${app.base-url}") private val baseUrl: String,
) : MemberActivationEmailSender {

    override fun sendActivationEmail(memberId: Long, email: Email, nickname: Nickname) {
        val activationToken = tokenGenerator.generate(memberId, expirationHours)
        val activationLink = "$baseUrl/members/activate?token=${activationToken.token}"
        val content = emailTemplate.buildActivationEmail(
            nickname = nickname.value,
            activationLink = activationLink,
            expirationHours = expirationHours,
        )
        emailSender.send(
            to = email,
            subject = "[RealMoneyRealTaste] 이메일 인증 안내",
            content = content,
            isHtml = true,
        )
    }
}
