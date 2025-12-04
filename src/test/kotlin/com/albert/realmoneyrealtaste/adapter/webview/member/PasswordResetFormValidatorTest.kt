package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordResetForm
import com.albert.realmoneyrealtaste.adapter.webview.member.validator.PasswordResetFormValidator
import org.springframework.validation.BeanPropertyBindingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordResetFormValidatorTest {

    private val validator = PasswordResetFormValidator()

    @Test
    fun `supports - success - returns true for PasswordResetForm class`() {
        val result = validator.supports(PasswordResetForm::class.java)

        assertTrue(result)
    }

    @Test
    fun `supports - success - returns false for other classes`() {
        val result = validator.supports(Any::class.java)

        assertFalse(result)
    }

    @Test
    fun `validate - success - no error when passwords match`() {
        // Given
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = "NewPassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertFalse(errors.hasErrors())
    }

    @Test
    fun `validate - failure - detects password mismatch and adds error`() {
        // Given
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = "DifferentPassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasErrors())
        assertEquals(1, errors.errorCount)

        val fieldError = errors.getFieldError("newPasswordConfirm")
        assertEquals("passwordMismatch", fieldError?.code)
        assertEquals("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", fieldError?.defaultMessage)
    }

    @Test
    fun `validate - failure - detects empty password confirmation`() {
        // Given
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = ""
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasErrors())
        assertEquals(1, errors.errorCount)

        val fieldError = errors.getFieldError("newPasswordConfirm")
        assertEquals("passwordMismatch", fieldError?.code)
        assertEquals("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", fieldError?.defaultMessage)
    }

    @Test
    fun `validate - failure - detects null password confirmation`() {
        // Given
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = "null"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasErrors())
        assertEquals(1, errors.errorCount)

        val fieldError = errors.getFieldError("newPasswordConfirm")
        assertEquals("passwordMismatch", fieldError?.code)
        assertEquals("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", fieldError?.defaultMessage)
    }

    @Test
    fun `validate - success - handles complex matching passwords`() {
        // Given - 복잡한 비밀번호 패턴이 일치하는 경우
        val form = PasswordResetForm(
            newPassword = "Complex@Pass123",
            newPasswordConfirm = "Complex@Pass123"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertFalse(errors.hasErrors())
    }

    @Test
    fun `validate - failure - case sensitive password mismatch`() {
        // Given - 대소문자가 다른 경우
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = "newpassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasErrors())
        assertEquals(1, errors.errorCount)

        val fieldError = errors.getFieldError("newPasswordConfirm")
        assertEquals("passwordMismatch", fieldError?.code)
        assertEquals("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", fieldError?.defaultMessage)
    }

    @Test
    fun `validate - failure - slight difference in passwords`() {
        // Given - 미세한 차이가 있는 경우
        val form = PasswordResetForm(
            newPassword = "NewPassword123!",
            newPasswordConfirm = "NewPassword123"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasErrors())
        assertEquals(1, errors.errorCount)

        val fieldError = errors.getFieldError("newPasswordConfirm")
        assertEquals("passwordMismatch", fieldError?.code)
        assertEquals("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", fieldError?.defaultMessage)
    }

    @Test
    fun `validate - success - handles special characters in matching passwords`() {
        // Given - 특수문자가 포함된 일치하는 비밀번호
        val form = PasswordResetForm(
            newPassword = "!@#\$Password123ABC",
            newPasswordConfirm = "!@#\$Password123ABC"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertFalse(errors.hasErrors())
    }

    @Test
    fun `validate - success - handles numeric passwords`() {
        // Given - 숫자가 포함된 일치하는 비밀번호
        val form = PasswordResetForm(
            newPassword = "Password123456!",
            newPasswordConfirm = "Password123456!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertFalse(errors.hasErrors())
    }

    @Test
    fun `validate - integration - works with BeanPropertyBindingResult`() {
        // Given - 실제 Spring BindingResult와의 통합 테스트
        val form = PasswordResetForm(
            newPassword = "TestPassword123!",
            newPasswordConfirm = "DifferentPassword123!"
        )
        val errors = BeanPropertyBindingResult(form, "passwordResetForm")

        // When
        validator.validate(form, errors)

        // Then
        assertTrue(errors.hasFieldErrors("newPasswordConfirm"))
        assertFalse(errors.hasFieldErrors("newPassword"))
        assertEquals("passwordMismatch", errors.getFieldError("newPasswordConfirm")?.code)
    }
}
