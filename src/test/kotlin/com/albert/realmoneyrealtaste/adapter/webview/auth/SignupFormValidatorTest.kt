package com.albert.realmoneyrealtaste.adapter.webview.auth

import org.springframework.validation.BeanPropertyBindingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignupFormValidatorTest {

    private val validator = SignupFormValidator()

    @Test
    fun `supports - success - returns true for SignupForm class`() {
        val result = validator.supports(SignupForm::class.java)

        assertTrue(result)
    }

    @Test
    fun `supports - success - returns false for other classes`() {
        val result = validator.supports(Any::class.java)

        assertFalse(result)
    }

    @Test
    fun `validate - failure - detects password mismatch and adds error`() {
        val form = SignupForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password2!"
        )
        val errors = BeanPropertyBindingResult(form, "memberRegisterForm")

        validator.validate(form, errors)

        assertTrue(errors.hasFieldErrors("confirmPassword"))
        assertEquals("passwordMismatch", errors.getFieldError("confirmPassword")?.code)
        assertEquals("비밀번호가 일치하지 않습니다.", errors.getFieldError("confirmPassword")?.defaultMessage)
    }

    @Test
    fun `validate - success - passes when passwords match`() {
        val form = SignupForm(
            email = "albert@gmail.com",
            nickname = "albert",
            password = "Password1!",
            confirmPassword = "Password1!"
        )
        val errors = BeanPropertyBindingResult(form, "memberRegisterForm")

        validator.validate(form, errors)

        assertFalse(errors.hasErrors())
    }
}
