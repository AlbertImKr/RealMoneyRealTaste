package com.albert.realmoneyrealtaste.adapter.webview.member.util

import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * Member 관련 유틸리티 함수
 */
object MemberUtils {

    /**
     * 성공 메시지를 Flash Attribute로 설정
     */
    fun setSuccessFlashAttribute(
        redirectAttributes: RedirectAttributes,
        message: String,
    ) {
        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", message)
    }

    /**
     * 에러 메시지를 Flash Attribute로 설정
     */
    fun setErrorFlashAttribute(
        redirectAttributes: RedirectAttributes,
        errorMessage: String,
    ) {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", errorMessage)
    }

    /**
     * 설정 페이지 에러 처리 (탭 포함 리다이렉트)
     */
    fun handleSettingError(
        redirectAttributes: RedirectAttributes,
        tab: String,
        errorMessage: String,
        redirectUrl: String,
    ): String {
        setErrorFlashAttribute(redirectAttributes, errorMessage)
        return "redirect:$redirectUrl#$tab"
    }

    /**
     * 설정 페이지 성공 처리 (탭 포함 리다이렉트)
     */
    fun handleSettingSuccess(
        redirectAttributes: RedirectAttributes,
        tab: String,
        successMessage: String,
        redirectUrl: String,
    ): String {
        setSuccessFlashAttribute(redirectAttributes, successMessage)
        return "redirect:$redirectUrl#$tab"
    }
}
