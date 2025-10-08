package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmailTemplateTest(
    val emailTemplate: EmailTemplate,
) : IntegrationTestBase() {

    @Test
    fun `buildActivationEmail - success - returns non-empty HTML content`() {
        val nickname = "테스터"
        val activationLink = "https://example.com/activate?token=abc123"
        val expirationHours = 24L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("<") && result.contains(">")) // HTML 태그 확인
    }

    @Test
    fun `buildActivationEmail - success - includes nickname in content`() {
        val nickname = "홍길동"
        val activationLink = "https://example.com/activate?token=abc123"
        val expirationHours = 24L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        assertContains(result, nickname)
    }

    @Test
    fun `buildActivationEmail - success - includes activation link in content`() {
        val nickname = "테스터"
        val activationLink = "https://example.com/activate?token=test-token-123"
        val expirationHours = 24L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        assertContains(result, activationLink)
    }

    @Test
    fun `buildActivationEmail - success - includes expiration hours in content`() {
        val nickname = "테스터"
        val activationLink = "https://example.com/activate?token=abc123"
        val expirationHours = 48L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        assertContains(result, expirationHours.toString())
    }

    @Test
    fun `buildActivationEmail - success - handles special characters in nickname`() {
        val nickname = "테스터<script>alert('xss')</script>"
        val activationLink = "https://example.com/activate?token=abc123"
        val expirationHours = 24L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        assertNotNull(result)
        // Thymeleaf는 기본적으로 XSS를 방지하므로 <script> 태그가 이스케이프되어야 함
        assertTrue(result.contains("&lt;script&gt;") || !result.contains("<script>"))
    }

    @Test
    fun `buildActivationEmail - success - generates valid HTML structure`() {
        val nickname = "테스터"
        val activationLink = "https://example.com/activate?token=abc123"
        val expirationHours = 24L

        val result = emailTemplate.buildActivationEmail(nickname, activationLink, expirationHours)

        // 기본 HTML 구조 확인
        assertTrue(result.contains("<!DOCTYPE") || result.contains("<html"))
        assertTrue(result.contains("</html>"))
    }
}
