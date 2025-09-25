package com.albert.realmoneyrealtaste.domain.member

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class EmailTest {

    @Test
    fun testValidEmail() {
        Assertions.assertThatNoException().isThrownBy { Email("valid123@naver.com") }
    }

    @Test
    fun testInvalidEmail_Blank() {
        Assertions.assertThat(Assertions.catchThrowable { Email("") })
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("이메일은 필수입니다")
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
    fun testInvalidEmail_Format(invalidEmail: String) {

        Assertions.assertThat(Assertions.catchThrowable { Email(invalidEmail) })
            .isInstanceOf(IllegalArgumentException::class.java)
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
    fun testGetDomain(emailStr: String, expectedDomain: String) {
        val email = Email(emailStr)

        Assertions.assertThat(email.getDomain()).isEqualTo(expectedDomain)
    }
}
