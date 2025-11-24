package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

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
    @ResponseBody
    fun follow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
    ): ResponseEntity<String> {
        followCreator.follow(
            FollowCreateRequest(
                followerId = principal.id,
                followingId = targetId,
            )
        )

        // 언팔로우 버튼 HTML 조각 반환
        val followButtonHtml = """
            <button class="btn btn-primary rounded-circle icon-md ms-auto"
                    hx-delete="/members/$targetId/follow"
                    hx-target="#follow-button-$targetId"
                    hx-swap="innerHTML">
                <i class="bi bi-person-check-fill"></i>
            </button>
        """.trimIndent()

        return ResponseEntity.ok(followButtonHtml)
    }
}
