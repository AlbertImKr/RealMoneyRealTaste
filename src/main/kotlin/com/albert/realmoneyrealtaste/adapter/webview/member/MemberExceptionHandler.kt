package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberDeactivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import com.albert.realmoneyrealtaste.application.member.exception.MemberUpdateException
import com.albert.realmoneyrealtaste.application.member.exception.PassWordResetException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@ControllerAdvice(annotations = [Controller::class])
class MemberExceptionHandler {

    @ExceptionHandler(MemberActivateException::class)
    fun handleMemberActivateException(
        ex: MemberActivateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "회원 활성화에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MemberUrls.ACTIVATION}"
    }

    @ExceptionHandler(MemberResendActivationEmailException::class)
    fun handleMemberResendActivationEmailException(
        ex: MemberResendActivationEmailException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "활성화 이메일 재전송에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MemberUrls.RESEND_ACTIVATION}"
    }

    @ExceptionHandler(MemberUpdateException::class)
    fun handleMemberUpdateException(
        ex: MemberUpdateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "회원 정보 수정에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MemberUrls.SETTING}#account"
    }

    @ExceptionHandler(MemberDeactivateException::class)
    fun handleMemberDeactivateException(
        ex: MemberDeactivateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "회원 탈퇴에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${MemberUrls.SETTING}#delete"
    }

    @ExceptionHandler(PassWordResetException::class)
    fun handlePassWordResetException(
        ex: PassWordResetException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "비밀번호 재설정에 실패했습니다. 다시 시도해주세요.")

        return "redirect:/"
    }
}
