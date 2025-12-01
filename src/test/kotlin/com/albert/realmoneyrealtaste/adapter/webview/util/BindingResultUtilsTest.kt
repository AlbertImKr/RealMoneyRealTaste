package com.albert.realmoneyrealtaste.adapter.webview.util

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import kotlin.test.Test
import kotlin.test.assertEquals

class BindingResultUtilsTest {

    @Test
    fun `extractFirstErrorMessage - success - returns empty string when no errors exist`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - returns the error message when single error exists`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(
            FieldError(
                "test",
                "field1",
                "invalid value",
                false,
                arrayOf("field1.error"),
                null,
                "field1.error"
            )
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("field1.error", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - returns the first error message when multiple errors exist`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(
            FieldError(
                "test",
                "field1",
                "invalid value",
                false,
                arrayOf("field1.error"),
                null,
                "field1.error"
            )
        )
        bindingResult.addError(
            FieldError(
                "test",
                "field2",
                "invalid value",
                false,
                arrayOf("field2.error"),
                null,
                "field2.error"
            )
        )
        bindingResult.addError(
            FieldError(
                "test",
                "field3",
                "invalid value",
                false,
                arrayOf("field3.error"),
                null,
                "field3.error"
            )
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("field1.error", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - skips null error messages and finds next valid message`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(FieldError("test", "field1", "invalid value", false, null, null, null))
        bindingResult.addError(
            FieldError(
                "test",
                "field2",
                "invalid value",
                false,
                arrayOf("field2.error"),
                null,
                "field2.error"
            )
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("field2.error", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - returns empty string when all error messages are null`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(FieldError("test", "field1", "invalid value", false, null, null, null))
        bindingResult.addError(FieldError("test", "field2", "invalid value", false, null, null, null))

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - returns custom error message when provided`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(
            FieldError("test", "email", "invalid-email", false, arrayOf("email.format"), null, "올바른 이메일 형식이 아닙니다")
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("올바른 이메일 형식이 아닙니다", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - extracts message from ObjectError`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(ObjectError("test", "전체 객체 에러 메시지"))

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("전체 객체 에러 메시지", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - returns first error when FieldError and ObjectError are mixed`() {
        // Given
        val bindingResult = BeanPropertyBindingResult("test", "test")
        bindingResult.addError(
            FieldError(
                "test",
                "field1",
                "invalid",
                false,
                arrayOf("field1.error"),
                null,
                "첫 번째 필드 에러"
            )
        )
        bindingResult.addError(ObjectError("test", "객체 에러"))

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("첫 번째 필드 에러", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - handles real email validation error scenario`() {
        // Given - 실제 이메일 폼 검증 시나리오
        val bindingResult = BeanPropertyBindingResult("passwordResetEmailForm", "passwordResetEmailForm")
        bindingResult.addError(
            FieldError(
                "passwordResetEmailForm",
                "email",
                "invalid-email",
                false,
                arrayOf("Email"),
                null,
                "올바른 이메일 형식이 아닙니다"
            )
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("올바른 이메일 형식이 아닙니다", result)
    }

    @Test
    fun `extractFirstErrorMessage - success - handles real password validation error scenario`() {
        // Given - 실제 비밀번호 폼 검증 시나리오
        val bindingResult = BeanPropertyBindingResult("passwordResetForm", "passwordResetForm")
        bindingResult.addError(
            FieldError(
                "passwordResetForm",
                "newPassword",
                "weak",
                false,
                arrayOf("Size"),
                arrayOf(8, 20),
                "비밀번호는 최소 8자 이상이어야 합니다"
            )
        )
        bindingResult.addError(
            FieldError(
                "passwordResetForm",
                "newPasswordConfirm",
                "",
                false,
                arrayOf("NotEmpty"),
                null,
                "비밀번호 확인은 필수입니다"
            )
        )

        // When
        val result = BindingResultUtils.extractFirstErrorMessage(bindingResult)

        // Then
        assertEquals("비밀번호는 최소 8자 이상이어야 합니다", result)
    }
}
