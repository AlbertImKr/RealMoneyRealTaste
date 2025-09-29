package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.MemberCommandService
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegisterRequest
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class MemberView(
    private val memberCommandService: MemberCommandService,
    private val validator: MemberRegisterFormValidator,
) {

    @GetMapping("/members/new")
    fun registerForm(model: Model): String {
        model.addAttribute("memberRegisterForm", MemberRegisterForm())
        return NEW_VIEW_NAME
    }

    @PostMapping("/members")
    fun register(
        @Valid form: MemberRegisterForm,
        bindingResult: BindingResult,
    ): String {
        validator.validate(form, bindingResult)

        if (bindingResult.hasErrors()) {
            return NEW_VIEW_NAME
        }

        val request = MemberRegisterRequest(
            email = Email(form.email),
            password = RawPassword(form.password),
            nickname = Nickname(form.nickname)
        )

        memberCommandService.register(request)

        return "redirect:/"
    }

    companion object {
        private const val NEW_VIEW_NAME = "member/new"
    }
}
