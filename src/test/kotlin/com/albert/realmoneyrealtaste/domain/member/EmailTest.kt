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
    fun `test valid email`() {
        val validEmailAddress = "valid123@naver.com"

        val email = Email(validEmailAddress)

        assertEquals(validEmailAddress, email.address)
    }

    @Test
    fun `test invalid email - empty`() {
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
    fun `test invalid email - format`(invalidEmail: String) {
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
    fun `test get domain from email`(emailStr: String, expectedDomain: String) {
        val email = Email(emailStr)

        val domain = email.getDomain()

        assertEquals(expectedDomain, domain)
    }
}
