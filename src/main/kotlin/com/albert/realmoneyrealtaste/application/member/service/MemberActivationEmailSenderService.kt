package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.MemberActivationEmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailTemplate
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MemberActivationEmailSenderService(
    private val emailTemplate: EmailTemplate,
    private val emailSender: EmailSender,
    @param:Value("\${app.member.activation-token.expiration-hours}") private val activationExpirationHours: Long,
    @param:Value("\${app.base-url}") private val baseUrl: String,
) : MemberActivationEmailSender {

    override fun sendActivationEmail(email: Email, nickname: Nickname, activationToken: ActivationToken) {
        val activationLink = createActivationLink(activationToken)

        val content = emailTemplate.buildActivationEmail(nickname.value, activationLink, activationExpirationHours)

        emailSender.send(email, ACTIVATION_EMAIL_SUBJECT, content, true)
    }

    /**
     * 회원 활성화 링크 생성
     */
    private fun createActivationLink(activationToken: ActivationToken): String {
        return "${baseUrl}${ACTIVATION_PATH}${activationToken.token}"
    }

    companion object {
        const val ACTIVATION_PATH = "/members/activate?token="
        const val ACTIVATION_EMAIL_SUBJECT = "[RealMoneyRealTaste] 이메일 인증 안내"
    }
}
