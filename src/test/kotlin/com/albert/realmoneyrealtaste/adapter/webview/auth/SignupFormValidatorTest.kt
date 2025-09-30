package com.albert.realmoneyrealtaste.adapter.webview.auth

import org.springframework.validation.BeanPropertyBindingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignupFormValidatorTest {

    private val validator = SignupFormValidator()

    @Test
    fun `supports SignupForm class`() {
        assertTrue { validator.supports(SignupForm::class.java) }
    }

    @Test
    fun `does not support other classes`() {
        assertTrue { !validator.supports(Any::class.java) }
    }

    @Test
    fun `validate detects password mismatch`() {
        val form = SignupForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password2!"
        )
        val errors = BeanPropertyBindingResult(form, "memberRegisterForm")

        validator.validate(form, errors)

        assertTrue { errors.hasFieldErrors("confirmPassword") }
        assertEquals("passwordMismatch", errors.getFieldError("confirmPassword")?.code)
        assertEquals("비밀번호가 일치하지 않습니다.", errors.getFieldError("confirmPassword")?.defaultMessage)
    }

    @Test
    fun `validate passes when passwords match`() {
        val form = SignupForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password1!"
        )
        val errors = BeanPropertyBindingResult(form, "memberRegisterForm")

        validator.validate(form, errors)

        assertTrue { !errors.hasErrors() }
    }
}
