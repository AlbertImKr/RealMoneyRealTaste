package com.albert.realmoneyrealtaste.adapter.webview.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SigninForm(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String = "",

    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
        message = "비밀번호는 대문자, 소문자, 숫자, 특수문자(!@#\$%^&*)를 각각 최소 한 개 이상 포함해야 합니다."
    )
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    val password: String = "",
)
