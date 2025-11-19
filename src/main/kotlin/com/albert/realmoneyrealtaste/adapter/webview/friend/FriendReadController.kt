package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Friends 뷰 컨트롤러
 */
@Controller
@RequestMapping
class FriendReadController(
    private val friendshipReader: FriendshipReader,
) {

    /**
     * Friends 프래그먼트 조회
     */
    @GetMapping(FriendUrls.FRIENDS_FRAGMENT)
    fun readFriendsFragment(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val targetMemberId = principal.memberId

        val friends = if (keyword.isNullOrBlank()) {
            friendshipReader.findFriendsByMemberId(
                memberId = targetMemberId,
                pageable = pageable
            )
        } else {
            friendshipReader.searchFriends(
                memberId = targetMemberId,
                keyword = keyword,
                pageable = pageable
            )
        }

        model.addAttribute("friends", friends)
        model.addAttribute("member", principal)

        return FriendViews.FRIENDS_FRAGMENT
    }

    /**
     * Friends 전체 페이지 조회
     */
    @GetMapping(FriendUrls.FRIENDS)
    fun readFriendsPage(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val targetMemberId = principal.memberId

        val friends = if (keyword.isNullOrBlank()) {
            friendshipReader.findFriendsByMemberId(
                memberId = targetMemberId,
                pageable = pageable
            )
        } else {
            friendshipReader.searchFriends(
                memberId = targetMemberId,
                keyword = keyword,
                pageable = pageable
            )
        }

        // 최근 친구 목록 (사이드바용)
        val recentFriends = friendshipReader.findRecentFriends(
            memberId = targetMemberId,
            limit = 6
        )

        model.addAttribute("friends", friends)
        model.addAttribute("recentFriends", recentFriends)
        model.addAttribute("member", principal)

        return FriendViews.FRIENDS_LIST
    }

    /**
     * 특정 사용자의 Friends 조회
     */
    @GetMapping(FriendUrls.MEMBER_FRIENDS)
    fun readMemberFriends(
        @PathVariable memberId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        model: Model,
    ): String {
        val friends = if (keyword.isNullOrBlank()) {
            friendshipReader.findFriendsByMemberId(
                memberId = memberId,
                pageable = pageable
            )
        } else {
            friendshipReader.searchFriends(
                memberId = memberId,
                keyword = keyword,
                pageable = pageable
            )
        }

        // 최근 친구 목록 (사이드바용)
        val recentFriends = friendshipReader.findRecentFriends(
            memberId = memberId,
            limit = 6
        )

        model.addAttribute("friends", friends)
        model.addAttribute("recentFriends", recentFriends)
        model.addAttribute("targetMemberId", memberId)
        model.addAttribute("member", principal)

        return FriendViews.MEMBER_FRIENDS_LIST
    }
}
