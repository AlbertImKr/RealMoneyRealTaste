package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.PostCreateForm
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberActivate
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberUpdater
import com.albert.realmoneyrealtaste.application.member.provided.PasswordResetter
import com.albert.realmoneyrealtaste.domain.member.value.Email
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MemberView(
    private val memberActivate: MemberActivate,
    private val memberReader: MemberReader,
    private val memberUpdater: MemberUpdater,
    private val validator: PasswordUpdateFormValidator,
    private val passwordResetter: PasswordResetter,
    private val followReader: FollowReader,
    private val friendshipReader: FriendshipReader,
) {

    @GetMapping(MemberUrls.PROFILE)
    fun readProfile(
        @PathVariable id: Long,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        model: Model,
    ): String {
        val profileMember = memberReader.readActiveMemberById(id)

        model.addAttribute("author", profileMember) // 프로필 주인
        model.addAttribute("member", memberPrincipal) // 현재 로그인한 사용자
        model.addAttribute("postCreateForm", PostCreateForm())

        // 팔로우 및 친구 관계 상태 확인
        if (memberPrincipal != null && memberPrincipal.memberId != id) {
            val followingIds = followReader.findFollowings(memberPrincipal.memberId, listOf(id))
            model.addAttribute("isFollowing", followingIds.contains(id))

            // 친구 관계 상태 확인
            val isFriend = friendshipReader.existsByMemberIds(memberPrincipal.memberId, id)
            model.addAttribute("isFriend", isFriend)

            // 친구 요청을 보냈는지 확인 (내가 보낸 요청이 대기 중인지)
            val friendship = friendshipReader.findFriendshipBetweenMembers(memberPrincipal.memberId, id)
            val hasSentFriendRequest = friendship != null
            model.addAttribute("hasSentFriendRequest", hasSentFriendRequest)
        }

        return MemberViews.PROFILE
    }

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

    @GetMapping(MemberUrls.RESEND_ACTIVATION)
    fun resendActivationEmail(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        model.addAttribute("email", memberPrincipal.email)
        return MemberViews.RESEND_ACTIVATION
    }

    @PostMapping(MemberUrls.RESEND_ACTIVATION)
    fun resendActivationEmail(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        redirectAttributes: RedirectAttributes,
    ): String {
        memberActivate.resendActivationEmail(memberPrincipal.email)

        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", "인증 이메일이 재발송되었습니다. 이메일을 확인해주세요.")

        return "redirect:${MemberUrls.RESEND_ACTIVATION}"
    }

    @GetMapping(MemberUrls.SETTING)
    fun setting(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        val member = memberReader.readMemberById(memberPrincipal.memberId)
        model.addAttribute("member", member)
        return MemberViews.SETTING
    }

    @PostMapping(MemberUrls.SETTING_ACCOUNT)
    fun updateAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute form: AccountUpdateForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("tab", "account")

        if (bindingResult.hasErrors()) {
            val errorMessages = bindingResult.fieldErrors.first().defaultMessage
            redirectAttributes.addFlashAttribute("error", errorMessages)
            return "redirect:${MemberUrls.SETTING}#account"
        }

        memberUpdater.updateInfo(memberPrincipal.memberId, form.toAccountUpdateRequest())

        redirectAttributes.addFlashAttribute("success", "계정 정보가 성공적으로 업데이트되었습니다.")
        return "redirect:${MemberUrls.SETTING}#account"
    }

    @PostMapping(MemberUrls.SETTING_PASSWORD)
    fun updatePassword(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @Valid @ModelAttribute request: PasswordUpdateForm,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("tab", "password")

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경이 실패했습니다. 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
            return "redirect:${MemberUrls.SETTING}#password"
        }

        validator.validate(request, bindingResult)

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경이 실패했습니다. 새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
            return "redirect:${MemberUrls.SETTING}#password"
        }

        memberUpdater.updatePassword(
            memberPrincipal.memberId, RawPassword(request.currentPassword), RawPassword(request.newPassword)
        )
        redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다.")

        return "redirect:${MemberUrls.SETTING}#password"
    }

    @PostMapping(MemberUrls.SETTING_DELETE)
    fun deleteAccount(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        @RequestParam confirmed: Boolean?,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        redirectAttributes.addFlashAttribute("tab", "delete")

        if (confirmed != true) {
            redirectAttributes.addFlashAttribute("error", "계정 삭제 확인이 필요합니다.")
            return "redirect:${MemberUrls.SETTING}#delete"
        }

        memberUpdater.deactivate(memberPrincipal.memberId)

        // 세션 무효화 및 로그아웃 처리
        request.session.invalidate()

        SecurityContextHolder.clearContext()

        return "redirect:/"
    }

    @GetMapping(MemberUrls.PASSWORD_FORGOT)
    fun passwordForgot(): String {
        return MemberViews.PASSWORD_FORGOT
    }

    @PostMapping(MemberUrls.PASSWORD_FORGOT)
    fun sendPasswordResetEmail(
        @RequestParam email: String,
        redirectAttributes: RedirectAttributes,
    ): String {

        val emailObj = try {
            Email(email)
        } catch (_: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("success", false)
            redirectAttributes.addFlashAttribute("error", "올바른 이메일 형식을 입력해주세요.")
            return "redirect:${MemberUrls.PASSWORD_FORGOT}"
        }

        passwordResetter.sendPasswordResetEmail(emailObj)

        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", "비밀번호 재설정 이메일이 발송되었습니다. 이메일을 확인해주세요.")

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
            redirectAttributes.addFlashAttribute("error", "비밀번호 형식이 올바르지 않습니다.")
            redirectAttributes.addFlashAttribute("token", token)
            return "redirect:${MemberUrls.PASSWORD_RESET}?token=$token"
        }

        if (form.newPassword != form.newPasswordConfirm) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
            redirectAttributes.addFlashAttribute("token", token)
            return "redirect:${MemberUrls.PASSWORD_RESET}?token=$token"
        }

        passwordResetter.resetPassword(token, RawPassword(form.newPassword))
        redirectAttributes.addFlashAttribute("success", true)
        redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 재설정되었습니다. 새로운 비밀번호로 로그인해주세요.")
        return "redirect:/"
    }

    @GetMapping(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
    fun readSidebarFragment(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal?,
        model: Model,
    ): String {
        if (memberPrincipal != null) {
            // 추천 사용자 목록 조회
            val suggestedUsers = memberReader.findSuggestedMembers(memberPrincipal.memberId, 5)
            val targetIds = suggestedUsers.map { it.requireId() }
            val followingIds = followReader.findFollowings(memberPrincipal.memberId, targetIds)

            model.addAttribute("suggestedUsers", suggestedUsers)
            model.addAttribute("followings", followingIds)
            model.addAttribute("member", memberPrincipal)
        }
        return MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT
    }
}
