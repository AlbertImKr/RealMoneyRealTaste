package com.albert.realmoneyrealtaste.config

import com.albert.realmoneyrealtaste.application.member.required.EmailSender
import com.albert.realmoneyrealtaste.domain.member.Email

data class SentEmail(
    val to: Email,
    val subject: String,
    val content: String,
    val isHtml: Boolean,
)

class TestEmailSender() : EmailSender {

    private val sentEmails = mutableListOf<SentEmail>()

    override fun send(
        to: Email,
        subject: String,
        content: String,
        isHtml: Boolean,
    ) {
        sentEmails.add(SentEmail(to, subject, content, isHtml))
    }

    fun getLastSentEmail(): SentEmail? = sentEmails.lastOrNull()

    fun clear() {
        sentEmails.clear()
    }

    fun count(): Int {
        return sentEmails.size
    }
}
