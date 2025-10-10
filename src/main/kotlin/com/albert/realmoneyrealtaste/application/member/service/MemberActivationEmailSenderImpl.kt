package com.albert.realmoneyrealtaste.application.member.service

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
        val activationLink = createActivationLink(memberId)

        val content = buildEmailContent(nickname, activationLink)

        emailSender.send(
            to = email,
            subject = ACTIVATION_EMAIL_SUBJECT,
            content = content,
            isHtml = true,
        )
    }

    /**
     * 이메일 본문 생성
     */
    private fun buildEmailContent(
        nickname: Nickname,
        activationLink: String,
    ): String = emailTemplate.buildActivationEmail(
        nickname = nickname.value,
        activationLink = activationLink,
        expirationHours = expirationHours,
    )

    /**
     * 회원 활성화 링크 생성
     */
    private fun createActivationLink(memberId: Long): String {
        val activationToken = tokenGenerator.generate(memberId, expirationHours)
        return "${baseUrl}${ACTIVATION_PATH}${activationToken.token}"
    }

    companion object {
        const val ACTIVATION_PATH = "/members/activate?token="
        const val ACTIVATION_EMAIL_SUBJECT = "[RealMoneyRealTaste] 이메일 인증 안내"
    }
}
