package com.albert.realmoneyrealtaste.adapter.webview.member.validator

import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordResetForm
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

/**
 * 비밀번호 재설정 폼 검증기 - 비밀번호 확인 일치 여부 검증
 */
@Component
class PasswordResetFormValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return PasswordResetForm::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val form = target as PasswordResetForm

        if (form.newPassword != form.newPasswordConfirm) {
            errors.rejectValue("newPasswordConfirm", "passwordMismatch", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        }
    }
}
