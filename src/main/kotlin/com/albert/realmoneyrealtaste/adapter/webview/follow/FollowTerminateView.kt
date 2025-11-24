package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.dto.UnfollowRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowTerminator
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

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
    @ResponseBody
    fun unfollow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
    ): ResponseEntity<String> {
        val request = try {
            UnfollowRequest(
                followerId = principal.id,
                followingId = targetId,
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(e.message)
        }

        followTerminator.unfollow(request)

        // 팔로우 버튼 HTML 조각 반환
        val followButtonHtml = """
            <button class="btn btn-primary-soft rounded-circle icon-md ms-auto"
                    hx-post="/members/$targetId/follow"
                    hx-target="#follow-button-$targetId"
                    hx-swap="innerHTML">
                <i class="fa-solid fa-plus"></i>
            </button>
        """.trimIndent()

        return ResponseEntity.ok(followButtonHtml)
    }
}
