package com.albert.realmoneyrealtaste.adapter.webview.member

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

@Component
class MemberRegisterFormValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return MemberRegisterForm::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val form = target as MemberRegisterForm

        if (form.password != form.confirmPassword) {
            errors.rejectValue("confirmPassword", "passwordMismatch", "비밀번호가 일치하지 않습니다.")
        }
    }
}
