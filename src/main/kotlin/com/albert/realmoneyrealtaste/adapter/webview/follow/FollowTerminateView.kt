package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.provided.FollowTerminator
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * 팔로우 삭제 API
 */
@Controller
class FollowTerminateView(
    private val followTerminator: FollowTerminator,
) {

    /**
     * 사용자 언팔로우
     */
    @DeleteMapping("/members/{targetId}/follow")
    fun unfollow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
        model: Model,
    ): String {
        followTerminator.unfollow(
            followerId = principal.id,
            followingId = targetId
        )

        model.addAttribute("authorId", targetId)
        model.addAttribute("isFollowing", false)

        return FollowViews.FOLLOW_CREATE_BUTTON
    }
}
