package com.albert.realmoneyrealtaste.adapter.webview.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class MemberRegisterForm(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String = "",

    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    val nickname: String = "",

    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    val password: String = "",

    @field:NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    val confirmPassword: String = "",
)
