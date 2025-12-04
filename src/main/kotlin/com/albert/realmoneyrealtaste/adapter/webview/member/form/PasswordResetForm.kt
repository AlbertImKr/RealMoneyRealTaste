package com.albert.realmoneyrealtaste.adapter.webview.member.form

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 비밀번호 재설정 폼
 */
data class PasswordResetForm(
    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*]).*$",
        message = "비밀번호는 숫자, 소문자, 대문자, 특수문자를 포함해야 합니다"
    )
    val newPassword: String,

    @field:NotBlank(message = "비밀번호 확인은 필수입니다")
    val newPasswordConfirm: String,
)
