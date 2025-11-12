package com.albert.realmoneyrealtaste.adapter.integration.email

import com.albert.realmoneyrealtaste.adapter.webview.member.MemberViews
import com.albert.realmoneyrealtaste.application.member.required.EmailTemplate
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailTemplateService(
    private val templateEngine: TemplateEngine,
) : EmailTemplate {

    override fun buildActivationEmail(nickname: String, activationLink: String, expirationHours: Long): String {
        val context = Context().apply {
            setVariable("nickname", nickname)
            setVariable("activationLink", activationLink)
            setVariable("expirationHours", expirationHours)
        }
        return templateEngine.process(MemberViews.ACTIVATION, context)
    }

    override fun buildPasswordResetEmail(nickname: String, passwordResetLink: String, expirationHours: Long): String {
        val context = Context().apply {
            setVariable("nickname", nickname)
            setVariable("passwordResetLink", passwordResetLink)
            setVariable("expirationHours", expirationHours)
        }
        return templateEngine.process(MemberViews.PASSWORD_RESET_EMAIL, context)
    }
}
