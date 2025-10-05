package com.albert.realmoneyrealtaste.domain.member

import org.assertj.core.api.Assertions.assertThatThrownBy
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

        assertFailsWith<IllegalArgumentException> {
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
        assertThatThrownBy {
            Email(invalidEmail)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("유효하지 않은 이메일 형식입니다")
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
