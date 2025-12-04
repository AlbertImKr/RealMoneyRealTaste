package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.converter.StringToEmailConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringToEmailConverterTest {

    private val converter = StringToEmailConverter()

    @Test
    fun `convert - success - converts valid email string to Email object`() {
        // Given
        val emailString = "test@example.com"

        // When
        val result = converter.convert(emailString)

        // Then
        assertEquals(emailString, result.address)
    }

    @Test
    fun `convert - success - handles email with subdomain`() {
        // Given
        val emailString = "user@mail.example.com"

        // When
        val result = converter.convert(emailString)

        // Then
        assertEquals(emailString, result.address)
    }

    @Test
    fun `convert - success - handles email with numbers`() {
        // Given
        val emailString = "user123@test456.com"

        // When
        val result = converter.convert(emailString)

        // Then
        assertEquals(emailString, result.address)
    }

    @Test
    fun `convert - success - handles email with special characters`() {
        // Given
        val emailString = "test.user+alias@example-domain.com"

        // When
        val result = converter.convert(emailString)

        // Then
        assertEquals(emailString, result.address)
    }

    @Test
    fun `convert - success - handles email with hyphens in domain`() {
        // Given
        val emailString = "user@my-domain.co.kr"

        // When
        val result = converter.convert(emailString)

        // Then
        assertEquals(emailString, result.address)
    }

    @Test
    fun `convert - success - handles simple email formats`() {
        // Given
        val testCases = listOf(
            "a@b.co",
            "ab@cd.com",
            "user@domain.io",
            "test@site.org"
        )

        testCases.forEach { emailString ->
            // When
            val result = converter.convert(emailString)

            // Then
            assertEquals(emailString, result.address)
        }
    }

    @Test
    fun `convert - failure - throws exception for empty string`() {
        // Given
        val emailString = ""

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("이메일은 필수입니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for blank string`() {
        // Given
        val emailString = "   "

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("이메일은 필수입니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email without domain`() {
        // Given
        val emailString = "user@"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email without local part`() {
        // Given
        val emailString = "@domain.com"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email without at symbol`() {
        // Given
        val emailString = "userdomain.com"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email with invalid domain`() {
        // Given
        val emailString = "user@invalid"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email with multiple at symbols`() {
        // Given
        val emailString = "user@domain@com"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - failure - throws exception for email with invalid characters`() {
        // Given
        val emailString = "user!@domain.com"

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            converter.convert(emailString)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @Test
    fun `convert - integration - preserves original string value`() {
        // Given
        val originalEmail = "original.test@example.com"

        // When
        val result = converter.convert(originalEmail)

        // Then
        assertEquals(originalEmail, result.address)
        assertEquals("example.com", result.getDomain())
    }

    @Test
    fun `convert - integration - works with edge case valid emails`() {
        // Given - 엣지 케이스지만 유효한 이메일들
        val validEmails = listOf(
            "a+b@c.co",
            "test_user@email-domain.org",
            "user123@sub.domain.net",
            "simple@simple.io"
        )

        validEmails.forEach { emailString ->
            // When
            val result = converter.convert(emailString)

            // Then
            assertEquals(emailString, result.address)
        }
    }
}
