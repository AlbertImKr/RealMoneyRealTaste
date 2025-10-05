package com.albert.realmoneyrealtaste.adapter.webview.auth

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SigninFormTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `constructor - success - creates form with default empty values`() {
        val form = SigninForm()

        assertAll(
            { assertEquals("", form.email) },
            { assertEquals("", form.password) }
        )
    }

    @Test
    fun `constructor - success - creates form with specific values`() {
        val email = "albert@gmail.com"
        val password = "Password1!"

        val form = SigninForm(
            email = email,
            password = password
        )

        assertAll(
            { assertEquals(email, form.email) },
            { assertEquals(password, form.password) }
        )
    }

    @Test
    fun `validate - failure - detects invalid email format`() {
        val form = SigninForm(
            email = "invalid-email",
            password = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val emailViolation = violations.find { it.propertyPath.toString() == "email" }
        assertEquals("이메일 형식이 올바르지 않습니다.", emailViolation?.message)
    }

    @Test
    fun `validate - failure - detects blank email`() {
        val form = SigninForm(
            email = "",
            password = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val emailViolation = violations.find { it.propertyPath.toString() == "email" }
        assertEquals("이메일은 필수 입력 항목입니다.", emailViolation?.message)
    }

    @Test
    fun `validate - failure - detects blank password`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = ""
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertTrue(passwordViolation?.message?.contains("비밀번호") == true)
    }

    @Test
    fun `validate - failure - detects short password`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "Pass1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", passwordViolation?.message)
    }

    @Test
    fun `validate - failure - detects long password`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "Password1!".repeat(3)
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", passwordViolation?.message)
    }

    @Test
    fun `validate - failure - detects password without letter`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "12345678!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate - failure - detects password without digit`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "Password!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate - failure - detects password without special character`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "Password1"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate - failure - detects multiple errors`() {
        val form = SigninForm(
            email = "invalid-email",
            password = "short"
        )

        val violations = validator.validate(form)

        assertTrue(violations.size >= 2)
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
        assertTrue(violations.any { it.propertyPath.toString() == "password" })
    }

    @Test
    fun `validate - success - passes with valid form`() {
        val form = SigninForm(
            email = "albert@gmail.com",
            password = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts all allowed special characters`() {
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')

        specialChars.forEach { char ->
            val form = SigninForm(
                email = "albert@gmail.com",
                password = "Password1$char"
            )

            val violations = validator.validate(form)

            assertTrue(violations.isEmpty(), "특수문자 '$char' 가 포함된 비밀번호가 유효하지 않음")
        }
    }
}
