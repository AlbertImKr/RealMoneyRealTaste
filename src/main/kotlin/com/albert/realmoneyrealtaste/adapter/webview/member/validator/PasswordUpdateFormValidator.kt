package com.albert.realmoneyrealtaste.adapter.webview.member.validator

import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordUpdateForm
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

/**
 * 비밀번호 변경 폼 검증기 - 새 비밀번호 확인 일치 여부 검증
 */
@Component
class PasswordUpdateFormValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return PasswordUpdateForm::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val form = target as PasswordUpdateForm

        if (form.newPassword != form.confirmNewPassword) {
            errors.rejectValue("confirmNewPassword", "passwordMismatch", "비밀번호가 일치하지 않습니다.")
        }
    }
}
