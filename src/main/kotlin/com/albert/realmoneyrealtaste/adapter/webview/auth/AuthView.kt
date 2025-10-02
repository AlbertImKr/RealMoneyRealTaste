package com.albert.realmoneyrealtaste.adapter.webview.auth

import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegisterRequest
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AuthView(
    private val memberRegister: MemberRegister,
    private val authenticationManager: AuthenticationManager,
    private val validator: SignupFormValidator,
) {

    @GetMapping("/signup")
    fun signupForm(model: Model): String {
        model.addAttribute("signupForm", SignupForm())
        return SIGNUP_VIEW_NAME
    }

    @PostMapping("/signup")
    fun signup(
        @Valid form: SignupForm,
        bindingResult: BindingResult,
    ): String {
        validator.validate(form, bindingResult)

        if (bindingResult.hasErrors()) {
            return SIGNUP_VIEW_NAME
        }

        val request = MemberRegisterRequest(
            email = Email(form.email),
            password = RawPassword(form.password),
            nickname = Nickname(form.nickname)
        )

        memberRegister.register(request)

        return "redirect:/signin"
    }

    @GetMapping("/signin")
    fun signinForm(model: Model): String {
        model.addAttribute("signinForm", SigninForm())
        return SIGNIN_VIEW_NAME
    }

    @PostMapping("/signin")
    fun signin(
        @Valid form: SigninForm,
        bindingResult: BindingResult,
    ): String {
        if (bindingResult.hasErrors()) {
            return SIGNIN_VIEW_NAME
        }

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(form.email, form.password)
        )

        return "redirect:/"
    }

    companion object {
        private const val SIGNUP_VIEW_NAME = "auth/signup"
        private const val SIGNIN_VIEW_NAME = "auth/signin"
    }
}
