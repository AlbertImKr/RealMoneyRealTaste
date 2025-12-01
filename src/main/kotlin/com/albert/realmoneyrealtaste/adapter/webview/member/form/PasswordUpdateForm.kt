package com.albert.realmoneyrealtaste.adapter.webview.member.form

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 비밀번호 변경 폼
 */
data class PasswordUpdateForm(
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    val currentPassword: String = "",

    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    val newPassword: String = "",

    @field:NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    val confirmNewPassword: String = "",
)
