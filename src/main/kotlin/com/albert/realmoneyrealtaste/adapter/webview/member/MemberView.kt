package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MemberView(
    private val memberActivate: MemberActivate,
    private val memberReader: MemberReader,
    private val memberUpdater: MemberUpdater,
    private val validator: PasswordUpdateFormValidator,
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
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        model.addAttribute("email", memberPrincipal.email)
        return MEMBER_RESEND_ACTIVATION_VIEW_NAME
    }

    @PostMapping("/members/resend-activation")
    fun resendActivationEmail(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        redirectAttributes: RedirectAttributes,
    ): String {
        memberActivate.resendActivationEmail(memberPrincipal.email)

        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", "인증 이메일이 재발송되었습니다. 이메일을 확인해주세요.")

        return "redirect:/members/resend-activation"
    }

    @GetMapping("/members/setting")
    fun setting(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        val member = memberReader.readMemberById(memberPrincipal.memberId)
        model.addAttribute("member", member)
        return MEMBER_SETTING_VIEW_NAME
    }

    @PostMapping("/members/setting/account")
    fun updateAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute accountUpdateRequest: AccountUpdateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력값을 확인해주세요.")
            return "redirect:/members/setting"
        }

        memberUpdater.updateInfo(memberPrincipal.memberId, accountUpdateRequest)
        redirectAttributes.addFlashAttribute("success", "계정 정보가 성공적으로 업데이트되었습니다.")
        return "redirect:${MEMBER_SETTING_URL}"
    }

    @PostMapping("/members/setting/password")
    fun updatePassword(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute request: PasswordUpdateForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        validator.validate(request, bindingResult)

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력값을 확인해주세요.")
            return "redirect:${MEMBER_SETTING_URL}"
        }

        try {
            memberUpdater.updatePassword(
                memberPrincipal.memberId, RawPassword(request.currentPassword), RawPassword(request.newPassword)
            )
            redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다.")
        } catch (_: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.")
        }

        return "redirect:${MEMBER_SETTING_URL}"
    }

    @PostMapping("/members/setting/delete")
    fun deleteAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @RequestParam confirmed: Boolean,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (!confirmed) {
            redirectAttributes.addFlashAttribute("error", "계정 삭제 확인이 필요합니다.")
            return "redirect:${MEMBER_SETTING_URL}"
        }

        memberUpdater.deactivate(memberPrincipal.memberId)
        return "redirect:/signout"
    }

    companion object {
        const val MEMBER_ACTIVATE_VIEW_NAME = "member/activate"
        const val MEMBER_ACTIVATION_VIEW_NAME = "member/activation"
        const val MEMBER_RESEND_ACTIVATION_VIEW_NAME = "member/resend-activation"
        const val MEMBER_SETTING_VIEW_NAME = "member/setting"

        const val MEMBER_SETTING_URL = "/members/setting"
    }
}
