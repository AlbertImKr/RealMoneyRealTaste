package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

/**
 * 팔로우 생성 API
 */
@Controller
class FollowCreateView(
    private val followCreator: FollowCreator,
) {

    /**
     * 사용자 팔로우
     */
    @PostMapping("/members/{targetId}/follow")
    fun follow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
        model: Model,
    ): String {
        followCreator.follow(
            FollowCreateRequest(
                followerId = principal.id,
                followingId = targetId,
            )
        )

        model.addAttribute("authorId", targetId)
        model.addAttribute("isFollowing", true)

        return FollowViews.FOLLOW_TERMINATE_BUTTON
    }
}
