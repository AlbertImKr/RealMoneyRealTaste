package com.albert.realmoneyrealtaste.application.member.service

import com.albert.realmoneyrealtaste.application.member.provided.MemberPasswordResetEmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.application.member.required.EmailTemplate
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MemberPasswordResetEmailSenderService(
    private val emailTemplate: EmailTemplate,
    private val emailSender: EmailSender,
    @param:Value("\${app.member.password-reset-token.expiration-hours}") private val resetExpirationHours: Long,
    @param:Value("\${app.base-url}") private val baseUrl: String,
) : MemberPasswordResetEmailSender {

    override fun sendResetEmail(email: Email, nickname: Nickname, passwordResetToken: PasswordResetToken) {
        val passwordResetLink = createPasswordResetLink(passwordResetToken)

        val content = emailTemplate.buildPasswordResetEmail(nickname.value, passwordResetLink, resetExpirationHours)

        emailSender.send(email, RESET_EMAIL_SUBJECT, content, true)
    }

    /**
     * 비밀번호 재설정 링크 생성
     */
    private fun createPasswordResetLink(passwordResetToken: PasswordResetToken): String =
        "${baseUrl}${RESET_PATH}${passwordResetToken.token}"

    companion object {
        const val RESET_PATH = "/members/password-reset?token="
        const val RESET_EMAIL_SUBJECT = "[RealMoneyRealTaste] 비밀번호 재설정 안내"
    }
}
