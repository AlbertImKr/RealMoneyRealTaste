package com.albert.realmoneyrealtaste.adapter.webview.auth

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

@Component
class SignupFormValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return SignupForm::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        val form = target as SignupForm

        if (form.password != form.confirmPassword) {
            errors.rejectValue("confirmPassword", "passwordMismatch", "비밀번호가 일치하지 않습니다.")
        }
    }
}
