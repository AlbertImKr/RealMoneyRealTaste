package com.albert.realmoneyrealtaste.adapter.integration

import com.albert.realmoneyrealtaste.adapter.webview.member.MemberView
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
        return templateEngine.process(MemberView.MEMBER_ACTIVATION_VIEW_NAME, context)
    }
}
