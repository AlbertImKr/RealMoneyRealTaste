package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.MemberView.Companion.MEMBER_ACTIVATION_URL
import com.albert.realmoneyrealtaste.adapter.webview.member.MemberView.Companion.MEMBER_SETTING_URL
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@ControllerAdvice
class MemberExceptionHandler {

    @ExceptionHandler(DuplicateProfileAddressException::class)
    fun handleDuplicateProfileAddress(
        ex: DuplicateProfileAddressException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "이미 사용 중인 프로필 주소입니다.")

        return "redirect:${MEMBER_SETTING_URL}"
    }

    @ExceptionHandler(MemberActivateException::class)
    fun handleMemberActivateException(
        ex: MemberActivateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "회원 활성화에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MEMBER_ACTIVATION_URL}"
    }

    @ExceptionHandler(MemberResendActivationEmailException::class)
    fun handleMemberResendActivationEmailException(
        ex: MemberResendActivationEmailException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "활성화 이메일 재전송에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MemberView.MEMBER_RESEND_ACTIVATION_URL}"
    }
}
