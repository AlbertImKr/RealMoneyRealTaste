package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.webview.member.message.MemberMessages
import com.albert.realmoneyrealtaste.adapter.webview.member.util.MemberUtils
import com.albert.realmoneyrealtaste.application.member.exception.MemberActivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberDeactivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.application.member.exception.MemberResendActivationEmailException
import com.albert.realmoneyrealtaste.application.member.exception.MemberUpdateException
import com.albert.realmoneyrealtaste.application.member.exception.PassWordResetException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 회원 관련 예외 핸들러
 */
@ControllerAdvice(annotations = [Controller::class])
class MemberExceptionHandler {
    /**
     * 회원 활성화 예외 처리
     */
    @ExceptionHandler(MemberActivateException::class)
    fun handleMemberActivateException(
        ex: MemberActivateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.MEMBER_ACTIVATE_FAILED
        )

        return "redirect:${MemberUrls.ACTIVATION}"
    }

    /**
     * 활성화 이메일 재전송 예외 처리
     */
    @ExceptionHandler(MemberResendActivationEmailException::class)
    fun handleMemberResendActivationEmailException(
        ex: MemberResendActivationEmailException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.RESEND_ACTIVATION_EMAIL_FAILED
        )

        return "redirect:${MemberUrls.RESEND_ACTIVATION}"
    }

    /**
     * 회원 정보 업데이트 예외 처리
     */
    @ExceptionHandler(MemberUpdateException::class)
    fun handleMemberUpdateException(
        ex: MemberUpdateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.MEMBER_UPDATE_FAILED
        )

        return "redirect:${MemberUrls.SETTING}#account"
    }

    /**
     * 회원 탈퇴 예외 처리
     */
    @ExceptionHandler(MemberDeactivateException::class)
    fun handleMemberDeactivateException(
        ex: MemberDeactivateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.MEMBER_DEACTIVATE_FAILED
        )

        return "redirect:${MemberUrls.SETTING}#delete"
    }

    /**
     * 비밀번호 재설정 예외 처리
     */
    @ExceptionHandler(PassWordResetException::class)
    fun handlePassWordResetException(
        ex: PassWordResetException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.PASSWORD_RESET_FAILED
        )

        return "redirect:/"
    }

    /**
     * 회원 찾기 실패 예외 처리
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFoundException(
        ex: MemberNotFoundException,
        redirectAttributes: RedirectAttributes,
    ): String {
        MemberUtils.setErrorFlashAttribute(
            redirectAttributes,
            MemberMessages.Error.MEMBER_NOT_FOUND
        )

        return "error/404"
    }
}
