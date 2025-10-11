package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.exceptions.EmailValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmailTest {

    @Test
    fun `constructor - success - creates email with valid address`() {
        val validEmailAddress = "valid123@naver.com"

        val email = Email(validEmailAddress)

        assertEquals(validEmailAddress, email.address)
    }

    @Test
    fun `constructor - failure - throws exception when email is empty`() {
        val emptyEmail = "   "

        assertFailsWith<EmailValidationException.Required> {
            Email(emptyEmail)
        }.let {
            assertEquals("이메일은 필수입니다", it.message)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "username@com",
            "username@domain..com"
        ]
    )
    fun `constructor - failure - throws exception when email format is invalid`(invalidEmail: String) {
        assertFailsWith<EmailValidationException.InvalidFormat> {
            Email(invalidEmail)
        }.let {
            assertEquals("유효한 이메일 형식이 아닙니다", it.message)
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "example123@naver.com, naver.com",
            "example123@gmail.com, gmail.com",
            "example123@kakao.com, kakao.com"
        ]
    )
    fun `getDomain - success - extracts domain from email address`(emailStr: String, expectedDomain: String) {
        val email = Email(emailStr)

        val domain = email.getDomain()

        assertEquals(expectedDomain, domain)
    }
}
