package com.albert.realmoneyrealtaste.adapter.webview.member

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberRegisterFormTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `create form with default values`() {
        val form = MemberRegisterForm()

        Assertions.assertAll(
            { assertEquals("", form.email) },
            { assertEquals("", form.nickname) },
            { assertEquals("", form.password) },
            { assertEquals("", form.confirmPassword) }
        )
    }

    @Test
    fun `create form with specific values`() {
        val email = "albert@gmail.com"
        val nickname = "albert"
        val password = "Password1!"
        val confirmPassword = "Password123!"
        val form = MemberRegisterForm(
            email = email,
            nickname = nickname,
            password = password,
            confirmPassword = confirmPassword
        )

        Assertions.assertAll(
            { assertEquals(email, form.email) },
            { assertEquals(nickname, form.nickname) },
            { assertEquals(password, form.password) },
            { assertEquals(confirmPassword, form.confirmPassword) }
        )
    }

    @Test
    fun `validate detects invalid email format`() {
        val form = MemberRegisterForm(
            email = "invalid-email",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val emailViolation = violations.find { it.propertyPath.toString() == "email" }
        assertEquals("이메일 형식이 올바르지 않습니다.", emailViolation?.message)
    }

    @Test
    fun `validate detects blank email`() {
        val form = MemberRegisterForm(
            email = "",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val emailViolation = violations.find { it.propertyPath.toString() == "email" }
        assertEquals("이메일은 필수 입력 항목입니다.", emailViolation?.message)
    }

    @Test
    fun `validate detects blank nickname`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "",
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임은 2자 이상 20자 이하로 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate detects short nickname`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "a",
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임은 2자 이상 20자 이하로 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate detects long nickname`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "a".repeat(21),
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임은 2자 이상 20자 이하로 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate detects blank password`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate detects password without letter`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "12345678!",
            confirmPassword = "12345678!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate detects password without digit`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password!",
            confirmPassword = "Password!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate detects password without special character`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1",
            confirmPassword = "Password1"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val passwordViolation = violations.find { it.propertyPath.toString() == "password" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", passwordViolation?.message)
    }

    @Test
    fun `validate detects blank confirmPassword`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = ""
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val confirmPasswordViolation = violations.find { it.propertyPath.toString() == "confirmPassword" }
        assertEquals("비밀번호 확인은 필수 입력 항목입니다.", confirmPasswordViolation?.message)
    }

    @Test
    fun `validate passes with valid form`() {
        val form = MemberRegisterForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate accepts all allowed special characters`() {
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')

        specialChars.forEach { char ->
            val form = MemberRegisterForm(
                email = "albert@gmail.com",
                nickname = "albert",
                password = "Password1$char",
                confirmPassword = "Password1$char"
            )

            val violations = validator.validate(form)

            assertTrue(violations.isEmpty(), "특수문자 '$char' 가 포함된 비밀번호가 유효하지 않음")
        }
    }
}
