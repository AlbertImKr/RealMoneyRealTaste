package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * 팔로우 뷰 컨트롤러
 * 팔로잉/팔로워 목록을 조회하는 웹 페이지를 제공합니다.
 */
@Controller
class FollowViewController(
    private val followReader: FollowReader,
    private val memberReader: MemberReader,
) {
    /**
     * 내 팔로잉 목록 조회
     */
    @GetMapping(FollowUrls.FOLLOWING_FRAGMENT)
    fun readFollowingList(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        // memberId가 없으면 현재 사용자의 ID를 사용
        val targetMemberId = principal.memberId

        val followings = if (keyword.isNullOrBlank()) {
            followReader.findFollowingsByMemberId(
                memberId = targetMemberId,
                pageable = pageRequest
            )
        } else {
            followReader.searchFollowings(
                memberId = targetMemberId,
                keyword = keyword,
                pageable = pageRequest
            )
        }

        // 현재 사용자가 팔로우하는 사용자 ID 목록 추가
        val followingIds = followings.content.map { it.followingId }
        model.addAttribute("followingIds", followingIds)
        model.addAttribute("followings", followings)
        model.addAttribute("member", principal)
        return FollowViews.FOLLOWING_FRAGMENT
    }

    /**
     * 내 팔로워 목록 조회
     */
    @GetMapping(FollowUrls.FOLLOWERS_FRAGMENT)
    fun readFollowerList(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val targetMemberId = principal.memberId

        val followers = if (keyword.isNullOrBlank()) {
            followReader.findFollowersByMemberId(
                memberId = targetMemberId,
                pageable = pageRequest
            )
        } else {
            followReader.searchFollowers(
                memberId = targetMemberId,
                keyword = keyword,
                pageable = pageRequest
            )
        }

        // 현재 사용자가 팔로우하는 사용자 ID 목록 추가
        val followerIds = followers.content.map { it.followerId }
        val currentUserFollowingIds = followReader.findFollowings(principal.memberId, followerIds)

        model.addAttribute("followers", followers)
        model.addAttribute("currentUserFollowingIds", currentUserFollowingIds)
        model.addAttribute("member", principal)
        return FollowViews.FOLLOWERS_FRAGMENT
    }

    /**
     * 특정 사용자의 팔로잉 목록 조회
     */
    @GetMapping(FollowUrls.USER_FOLLOWING_FRAGMENT)
    fun readUserFollowingList(
        @PathVariable memberId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val followings = followReader.findFollowingsByMemberId(
            memberId = memberId,
            pageable = pageRequest
        )

        val member = memberReader.readMemberById(memberId)

        model.addAttribute("followings", followings)
        model.addAttribute("targetMember", member)
        return FollowViews.FOLLOWING_FRAGMENT
    }

    /**
     * 특정 사용자의 팔로워 목록 조회
     */
    @GetMapping(FollowUrls.USER_FOLLOWERS_FRAGMENT)
    fun readUserFollowerList(
        @PathVariable memberId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageRequest: Pageable,
        model: Model,
    ): String {
        val followers = followReader.findFollowersByMemberId(
            memberId = memberId,
            pageable = pageRequest
        )

        val member = memberReader.readMemberById(memberId)

        model.addAttribute("followers", followers)
        model.addAttribute("targetMember", member)
        return FollowViews.FOLLOWERS_FRAGMENT
    }
}
