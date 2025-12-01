package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordUpdateForm
import com.albert.realmoneyrealtaste.adapter.webview.member.validator.PasswordUpdateFormValidator
import org.springframework.validation.BeanPropertyBindingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordUpdateFormValidatorTest {

    private val validator = PasswordUpdateFormValidator()

    @Test
    fun `supports - success - returns true for PasswordUpdateForm class`() {
        val result = validator.supports(PasswordUpdateForm::class.java)

        assertTrue(result)
    }

    @Test
    fun `supports - success - returns false for other classes`() {
        val result = validator.supports(Any::class.java)

        assertFalse(result)
    }

    @Test
    fun `validate - failure - detects password mismatch and adds error`() {
        val form = PasswordUpdateForm(
            currentPassword = "currentPassword123!",
            newPassword = "newPassword123!",
            confirmNewPassword = "differentPassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordUpdateForm")

        validator.validate(form, errors)

        assertTrue(errors.hasFieldErrors("confirmNewPassword"))
        assertEquals("passwordMismatch", errors.getFieldError("confirmNewPassword")?.code)
        assertEquals("비밀번호가 일치하지 않습니다.", errors.getFieldError("confirmNewPassword")?.defaultMessage)
    }

    @Test
    fun `validate - success - passes when passwords match`() {
        val form = PasswordUpdateForm(
            currentPassword = "currentPassword123!",
            newPassword = "newPassword123!",
            confirmNewPassword = "newPassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordUpdateForm")

        validator.validate(form, errors)

        assertFalse(errors.hasErrors())
    }

    @Test
    fun `validate - success - passes when new password and confirm are both empty`() {
        val form = PasswordUpdateForm(
            currentPassword = "currentPassword123!",
            newPassword = "",
            confirmNewPassword = ""
        )
        val errors = BeanPropertyBindingResult(form, "passwordUpdateForm")

        validator.validate(form, errors)

        assertFalse(errors.hasFieldErrors("confirmPassword"))
    }
}
