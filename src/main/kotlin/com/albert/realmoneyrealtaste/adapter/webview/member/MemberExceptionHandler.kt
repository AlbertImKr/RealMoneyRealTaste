package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.MemberView.Companion.MEMBER_SETTING_URL
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.domain.member.exceptions.AlreadyActivatedException
import com.albert.realmoneyrealtaste.domain.member.exceptions.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidActivationTokenException
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@ControllerAdvice
class MemberExceptionHandler {

    @ExceptionHandler(
        ExpiredActivationTokenException::class,
        InvalidActivationTokenException::class,
    )
    fun handleActivationTokenExceptions(
        ex: RuntimeException,
        model: Model,
    ): String {
        model.addAttribute("success", false)

        return MemberView.MEMBER_ACTIVATE_VIEW_NAME
    }

    @ExceptionHandler
    fun handleAlreadyActivated(
        ex: AlreadyActivatedException,
        model: Model,
    ): String {
        model.addAttribute("success", true)
        model.addAttribute("message", "이미 활성화된 회원입니다.")

        return MemberView.MEMBER_ACTIVATE_VIEW_NAME
    }

    @ExceptionHandler
    fun handleDuplicateProfileAddress(
        ex: DuplicateProfileAddressException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "이미 사용 중인 프로필 주소입니다.")

        return "redirect:${MEMBER_SETTING_URL}"
    }
}
