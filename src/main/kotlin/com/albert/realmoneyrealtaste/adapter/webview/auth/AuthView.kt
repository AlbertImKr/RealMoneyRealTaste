package com.albert.realmoneyrealtaste.adapter.webview.auth

import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
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

    @GetMapping(AuthUrls.SIGN_UP)
    fun signupForm(model: Model): String {
        model.addAttribute("signupForm", SignupForm())
        return AuthViews.SIGN_UP
    }

    @PostMapping(AuthUrls.SIGN_UP)
    fun signup(
        @Valid form: SignupForm,
        bindingResult: BindingResult,
    ): String {
        validator.validate(form, bindingResult)

        if (bindingResult.hasErrors()) {
            return AuthViews.SIGN_UP
        }

        val request = MemberRegisterRequest(
            email = Email(form.email),
            password = RawPassword(form.password),
            nickname = Nickname(form.nickname)
        )

        memberRegister.register(request)

        return "redirect:${AuthUrls.SIGN_IN}"
    }

    @GetMapping(AuthUrls.SIGN_IN)
    fun signinForm(model: Model): String {
        model.addAttribute("signinForm", SigninForm())
        return AuthViews.SIGN_IN
    }

    @PostMapping(AuthUrls.SIGN_IN)
    fun signin(
        @Valid form: SigninForm,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): String {
        if (bindingResult.hasErrors()) {
            return AuthViews.SIGN_IN
        }

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(form.email, form.password)
        )

        SecurityContextHolder.getContext().authentication = authentication

        request.session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        )

        return "redirect:/"
    }
}
