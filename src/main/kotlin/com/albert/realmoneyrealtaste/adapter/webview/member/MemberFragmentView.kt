package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * 회원 관련 프래그먼트 조각들 뷰 컨트롤러
 */
@Controller
class MemberFragmentView(
    private val memberReader: MemberReader,
) {
    /**
     * 추천 사용자 사이드바 프래그먼트
     */
    @GetMapping(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
    fun readSidebarFragment(
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
        model: Model,
    ): String {
        val result = memberReader.findSuggestedMembersWithFollowStatus(memberPrincipal.id, 5)

        model.addAttribute("suggestedUsers", result.suggestedUsers)
        model.addAttribute("followings", result.followingIds)
        model.addAttribute("member", memberPrincipal)
        return MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT
    }

    /**
     * 회원 프로필 프래그먼트
     */
    @GetMapping(MemberUrls.FRAGMENT_MEMBER_PROFILE)
    fun memberProfileFragment(
        model: Model,
        @AuthenticationPrincipal memberPrincipal: MemberPrincipal,
    ): String {
        val member = memberReader.readMemberById(memberPrincipal.id)

        model.addAttribute("member", member)
        model.addAttribute("followersCount", member.followersCount)
        model.addAttribute("followingCount", member.followingsCount)
        model.addAttribute("postCount", member.postCount)

        return MemberViews.MEMBER_PROFILE_FRAGMENT
    }
}
