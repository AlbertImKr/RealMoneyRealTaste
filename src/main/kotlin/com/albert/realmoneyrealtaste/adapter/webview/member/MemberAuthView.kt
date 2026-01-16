package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordResetEmailForm
import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordResetForm
import com.albert.realmoneyrealtaste.adapter.webview.member.message.MemberMessages
import com.albert.realmoneyrealtaste.adapter.webview.member.util.MemberUtils
import com.albert.realmoneyrealtaste.adapter.webview.member.validator.PasswordResetFormValidator
import com.albert.realmoneyrealtaste.adapter.webview.util.BindingResultUtils
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetter
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
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

/**
 * 회원 인증 및 비밀번호 재설정 관련 뷰 컨트롤러
 */
@Controller
class MemberAuthView(
    private val memberActivate: MemberActivate,
    private val passwordResetter: PasswordResetter,
    private val passwordResetFormValidator: PasswordResetFormValidator,
) {
    /**
     * 이메일 활성화 처리
     */
    @GetMapping(MemberUrls.ACTIVATION)
    fun activate(
        @RequestParam("token") token: String,
        model: Model,
    ): String {
        val member = memberActivate.activate(token)

        model.addAttribute("nickname", member.nickname.value)
        model.addAttribute("success", true)
        return MemberViews.ACTIVATE
    }

    /**
     * 활성화 이메일 재전송 폼
     */
    @GetMapping(MemberUrls.RESEND_ACTIVATION)
    fun resendActivationEmail(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        model.addAttribute("email", memberPrincipal.email)
        return MemberViews.RESEND_ACTIVATION
    }

    /**
     * 활성화 이메일 재전송 처리
     */
    @PostMapping(MemberUrls.RESEND_ACTIVATION)
    fun resendActivationEmail(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        redirectAttributes: RedirectAttributes,
    ): String {
        memberActivate.resendActivationEmail(memberPrincipal.email)

        MemberUtils.setSuccessFlashAttribute(
            redirectAttributes,
            MemberMessages.Auth.ACTIVATION_EMAIL_RESENT
        )

        return "redirect:${MemberUrls.RESEND_ACTIVATION}"
    }

    /**
     * 비밀번호 찾기 폼
     */
    @GetMapping(MemberUrls.PASSWORD_FORGOT)
    fun passwordForgot(): String {
        return MemberViews.PASSWORD_FORGOT
    }

    /**
     * 비밀번호 재설정 이메일 전송 처리
     */
    @PostMapping(MemberUrls.PASSWORD_FORGOT)
    fun sendPasswordResetEmail(
        @Valid @ModelAttribute form: PasswordResetEmailForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            val errorMessages = BindingResultUtils.extractFirstErrorMessage(bindingResult)
            MemberUtils.setErrorFlashAttribute(
                redirectAttributes,
                errorMessages
            )
            return "redirect:${MemberUrls.PASSWORD_FORGOT}"
        }

        passwordResetter.sendPasswordResetEmail(form.email)

        MemberUtils.setSuccessFlashAttribute(
            redirectAttributes,
            MemberMessages.Auth.PASSWORD_RESET_EMAIL_SENT
        )

        return "redirect:${MemberUrls.PASSWORD_FORGOT}"
    }

    @GetMapping(MemberUrls.PASSWORD_RESET)
    fun passwordReset(
        @RequestParam("token") token: String,
        model: Model,
    ): String {
        model.addAttribute("token", token)
        return MemberViews.PASSWORD_RESET
    }

    @PostMapping(MemberUrls.PASSWORD_RESET)
    fun resetPassword(
        @RequestParam token: String,
        @Valid @ModelAttribute form: PasswordResetForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            return handlePasswordResetErrors(bindingResult, token, redirectAttributes)
        }

        passwordResetFormValidator.validate(form, bindingResult)

        if (bindingResult.hasErrors()) {
            return handlePasswordResetErrors(bindingResult, token, redirectAttributes)
        }

        passwordResetter.resetPassword(token, RawPassword(form.newPassword))

        MemberUtils.setSuccessFlashAttribute(
            redirectAttributes,
            MemberMessages.Auth.PASSWORD_RESET_SUCCESS
        )
        return "redirect:/"
    }

    /**
     * 비밀번호 재설정 폼 검증 에러 처리
     */
    private fun handlePasswordResetErrors(
        bindingResult: BindingResult,
        token: String,
        redirectAttributes: RedirectAttributes,
    ): String {
        val errorMessages = BindingResultUtils.extractFirstErrorMessage(bindingResult)
        redirectAttributes.addFlashAttribute("token", token)
        redirectAttributes.addFlashAttribute("error", errorMessages)
        return "redirect:${MemberUrls.PASSWORD_RESET}?token=$token"
    }
}
