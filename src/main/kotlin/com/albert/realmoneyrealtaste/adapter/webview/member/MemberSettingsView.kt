package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.member.form.AccountUpdateForm
import com.albert.realmoneyrealtaste.adapter.webview.member.form.PasswordUpdateForm
import com.albert.realmoneyrealtaste.adapter.webview.member.message.MemberMessages
import com.albert.realmoneyrealtaste.adapter.webview.member.util.MemberUtils
import com.albert.realmoneyrealtaste.adapter.webview.member.validator.PasswordUpdateFormValidator
import com.albert.realmoneyrealtaste.adapter.webview.util.BindingResultUtils
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 회원 설정 관련 뷰 컨트롤러
 */
@Controller
class MemberSettingsView(
    private val memberReader: MemberReader,
    private val memberUpdater: MemberUpdater,
    private val validator: PasswordUpdateFormValidator,
) {

    /**
     * 회원 설정 페이지
     */
    @GetMapping(MemberUrls.SETTING)
    fun setting(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        val member = memberReader.readMemberById(memberPrincipal.id)

        model.addAttribute("member", member)
        return MemberViews.SETTING
    }

    /**
     * 계정 정보 업데이트 처리
     */
    @PostMapping(MemberUrls.SETTING_ACCOUNT)
    fun updateAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute form: AccountUpdateForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            val errorMessages = BindingResultUtils.extractFirstErrorMessage(bindingResult)
            return MemberUtils.handleSettingError(
                redirectAttributes, "account", errorMessages, MemberUrls.SETTING
            )
        }

        memberUpdater.updateInfo(memberPrincipal.id, form.toAccountUpdateRequest())

        return MemberUtils.handleSettingSuccess(
            redirectAttributes, "account", MemberMessages.Settings.ACCOUNT_UPDATE_SUCCESS, MemberUrls.SETTING
        )
    }

    /**
     * 비밀번호 변경 처리
     */
    @PostMapping(MemberUrls.SETTING_PASSWORD)
    fun updatePassword(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute request: PasswordUpdateForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            val errorMessages = BindingResultUtils.extractFirstErrorMessage(bindingResult)
            return MemberUtils.handleSettingError(
                redirectAttributes, "password", errorMessages, MemberUrls.SETTING
            )
        }

        validator.validate(request, bindingResult)

        if (bindingResult.hasErrors()) {
            val errorMessages = BindingResultUtils.extractFirstErrorMessage(bindingResult)
            return MemberUtils.handleSettingError(
                redirectAttributes, "password", errorMessages, MemberUrls.SETTING
            )
        }

        memberUpdater.updatePassword(
            memberPrincipal.id, RawPassword(request.currentPassword), RawPassword(request.newPassword)
        )

        return MemberUtils.handleSettingSuccess(
            redirectAttributes, "password", MemberMessages.Settings.PASSWORD_UPDATE_SUCCESS, MemberUrls.SETTING
        )
    }

    /**
     * 계정 삭제 처리
     */
    @PostMapping(MemberUrls.SETTING_DELETE)
    fun deleteAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @RequestParam confirmed: Boolean?,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        if (confirmed != true) {
            return MemberUtils.handleSettingError(
                redirectAttributes, "delete", MemberMessages.Settings.ACCOUNT_DELETE_NOT_CONFIRMED, MemberUrls.SETTING
            )
        }

        memberUpdater.deactivate(memberPrincipal.id)

        // 세션 무효화 및 로그아웃 처리
        request.session.invalidate()
        SecurityContextHolder.clearContext()

        return "redirect:/"
    }
}
