package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
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
class FriendReadView(
    private val friendshipReader: FriendshipReader,
    private val memberReader: MemberReader,
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
        val targetMemberId = principal.id

        val friends = findFriends(targetMemberId, keyword, pageable)

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
        val targetMemberId = principal.id

        val friends = findFriends(targetMemberId, keyword, pageable)

        // 최근 친구 목록 (사이드바용)
        val recentFriends = friendshipReader.findRecentFriends(
            memberId = targetMemberId,
            pageable = pageable
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
        val friends = findFriends(memberId, keyword, pageable)

        // 최근 친구 목록 (사이드바용)
        val recentFriends = friendshipReader.findRecentFriends(
            memberId = memberId,
            pageable,
        )

        model.addAttribute("friends", friends)
        model.addAttribute("recentFriends", recentFriends)
        model.addAttribute("targetMemberId", memberId)
        model.addAttribute("member", principal)

        return FriendViews.MEMBER_FRIENDS_LIST
    }

    /**
     * 친구 위젯 프래그먼트 조회
     */
    @GetMapping(FriendUrls.FRIEND_WIDGET)
    fun readFriendWidgetFragment(
        @PathVariable memberId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal?,
        model: Model,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): String {
        // 최근 친구 목록 (사이드바용)
        val recentFriends = friendshipReader.findRecentFriends(
            memberId = memberId,
            pageable,
        )

        // 대기 중인 친구 요청 개수
        val pendingRequestsCount = if (principal != null && principal.id == memberId) {
            friendshipReader.countPendingRequests(memberId)
        } else {
            0
        }

        val author = memberReader.readMemberById(memberId)

        model.addAttribute("recentFriends", recentFriends)
        model.addAttribute("pendingRequestsCount", pendingRequestsCount)
        model.addAttribute("author", author)
        model.addAttribute("member", principal)

        return FriendViews.FRIEND_WIDGET
    }

    /**
     * 친구 요청 리스트 프래그먼트 조회
     */
    @GetMapping(FriendUrls.FRIEND_REQUESTS_FRAGMENT)
    fun readFriendRequestsFragment(
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): String {
        // 대기 중인 친구 요청 목록
        val pendingRequests = friendshipReader.findPendingRequests(principal.id, pageable)

        model.addAttribute("pendingRequests", pendingRequests)
        model.addAttribute("member", principal)

        return FriendViews.FRIEND_REQUESTS
    }

    /**
     * 친구 목록을 조회하는 공통 메서드
     */
    private fun findFriends(memberId: Long, keyword: String?, pageable: Pageable) =
        if (keyword.isNullOrBlank()) {
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
}
