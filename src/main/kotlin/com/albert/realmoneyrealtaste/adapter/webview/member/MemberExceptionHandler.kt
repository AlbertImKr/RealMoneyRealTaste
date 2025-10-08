package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

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
}
