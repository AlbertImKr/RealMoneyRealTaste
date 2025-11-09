package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.MemberView.Companion.MEMBER_SETTING_URL
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@ControllerAdvice
class MemberExceptionHandler {

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
