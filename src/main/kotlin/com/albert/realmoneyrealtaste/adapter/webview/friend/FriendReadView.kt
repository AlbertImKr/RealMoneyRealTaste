package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
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

/**
 * Friends 뷰 컨트롤러
 */
@Controller
@RequestMapping
class FriendReadView(
    private val friendshipReader: FriendshipReader,
) {

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

        model.addAttribute("recentFriends", recentFriends)
        model.addAttribute("pendingRequestsCount", pendingRequestsCount)
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

    @GetMapping(FriendUrls.FRIEND_BUTTON)
    fun readFriendButton(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable authorId: Long,
        model: Model,
    ): String {
        val isFriend = friendshipReader.isFriend(principal.id, authorId)
        val hasSentFriendRequest = friendshipReader.isSent(principal.id, authorId)

        model.addAttribute("authorId", authorId)
        model.addAttribute("isFriend", isFriend)
        model.addAttribute("hasSentFriendRequest", hasSentFriendRequest)
        return FriendViews.FRIEND_BUTTON
    }
}
