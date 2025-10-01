package com.albert.realmoneyrealtaste.adapter.webview.auth

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(
        ex: BadCredentialsException,
        model: Model,
        request: HttpServletRequest,
    ): String {
        val email = request.getParameter("email")
        val signinForm = SigninForm(email = email ?: "", password = "")

        model.addAttribute("signinForm", signinForm)
        model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.")

        return "auth/signin"
    }
}
