package com.albert.realmoneyrealtaste.adapter.integration.email

import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.domain.member.Email
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class EmailSenderImpl(
    private val mailSender: JavaMailSender,
) : EmailSender {

    override fun send(
        to: Email,
        subject: String,
        content: String,
        isHtml: Boolean,
    ) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(to.address)
        helper.setSubject(subject)
        helper.setText(content, isHtml)

        mailSender.send(message)
    }
}
