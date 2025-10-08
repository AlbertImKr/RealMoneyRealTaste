package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.domain.member.Email
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MemberView(
    private val memberActivate: MemberActivate,
) {

    @GetMapping("/members/activate")
    fun activate(
        @RequestParam("token") token: String,
        model: Model,
    ): String {
        val member = memberActivate.activate(token)

        model.addAttribute("nickname", member.nickname.value)
        model.addAttribute("success", true)

        return MEMBER_ACTIVATE_VIEW_NAME
    }

    @GetMapping("/members/resend-activation")
    fun resendActivationEmail(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model,
    ): String {
        model.addAttribute("email", userDetails.username)
        return MEMBER_RESEND_ACTIVATION_VIEW_NAME
    }

    @PostMapping("/members/resend-activation")
    fun resendActivationEmail(
        @AuthenticationPrincipal userDetails: UserDetails,
        redirectAttributes: RedirectAttributes,
    ): String {
        memberActivate.resendActivationEmail(Email(userDetails.username))

        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", "인증 이메일이 재발송되었습니다. 이메일을 확인해주세요.")

        return "redirect:${MEMBER_RESEND_ACTIVATION_VIEW_NAME}"
    }

    companion object {
        const val MEMBER_ACTIVATE_VIEW_NAME = "member/activate"
        const val MEMBER_ACTIVATION_VIEW_NAME = "member/activation"
        const val MEMBER_RESEND_ACTIVATION_VIEW_NAME = "member/resend-activation"
    }
}
