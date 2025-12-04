package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.adapter.webview.post.form.PostCreateForm
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * 회원 프로필 조회 관련 뷰 컨트롤러
 */
@Controller
class MemberProfileView(
    private val memberReader: MemberReader,
) {
    /**
     * 회원 프로필 조회
     */
    @GetMapping(MemberUrls.PROFILE)
    fun readProfile(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        model: Model,
    ): String {
        val profileMember = memberReader.readActiveMemberById(id)

        model.addAttribute("author", profileMember) // 프로필 주인
        model.addAttribute("member", principal) // 현재 로그인한 사용자
        model.addAttribute("postCreateForm", PostCreateForm())
        return MemberViews.PROFILE
    }
}
