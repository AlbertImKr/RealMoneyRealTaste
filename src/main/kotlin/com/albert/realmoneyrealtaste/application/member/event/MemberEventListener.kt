package com.albert.realmoneyrealtaste.application.member.event

import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberEventListener(
    private val emailSender: EmailSender,
    private val emailTemplate: EmailTemplate,
    private val tokenGenerator: ActivationTokenGenerator,
    @param:Value("\${app.member.activation-token.expiration-hours}") private val expirationHours: Long,
    @param:Value("\${app.base-url}") private val baseUrl: String,
) {
    @Async
    @EventListener
    fun handleMemberRegistered(event: MemberRegisteredEvent) {

        val activationToken = tokenGenerator.generate(event.memberId, expirationHours)

        val activationLink = "$baseUrl/members/activate?token=${activationToken.token}"

        val content = emailTemplate.buildActivationEmail(
            nickname = event.nickname.value,
            activationLink = activationLink,
            expirationHours = expirationHours,
        )

        emailSender.send(
            to = event.email,
            subject = "[RealMoneyRealTaste] 이메일 인증 안내",
            content = content,
            isHtml = true,
        )
    }
}
