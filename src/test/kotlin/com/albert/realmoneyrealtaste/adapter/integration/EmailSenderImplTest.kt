package com.albert.realmoneyrealtaste.adapter.integration

import com.albert.realmoneyrealtaste.domain.member.Email
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.Test

class EmailSenderImplTest {

    private val mailSender = mockk<JavaMailSender>()
    private val emailSender = EmailSenderImpl(mailSender)

    @Test
    fun `send - success - send text email`() {
        val to = Email("test@example.com")
        val subject = "Test Subject"
        val content = "Test Content"
        val isHtml = false

        val mimeMessage = MimeMessage(null as Session?)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(any<MimeMessage>()) } just Runs

        emailSender.send(to, subject, content, isHtml)

        verify(exactly = 1) { mailSender.createMimeMessage() }
        verify(exactly = 1) { mailSender.send(mimeMessage) }
    }

    @Test
    fun `send - success - send html email`() {
        val to = Email("html@example.com")
        val subject = "HTML Email"
        val content = "<html><body><h1>Hello</h1></body></html>"
        val isHtml = true

        val mimeMessage = MimeMessage(null as Session?)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(any<MimeMessage>()) } just Runs

        emailSender.send(to, subject, content, isHtml)

        verify(exactly = 1) { mailSender.createMimeMessage() }
        verify(exactly = 1) { mailSender.send(mimeMessage) }
    }
}
