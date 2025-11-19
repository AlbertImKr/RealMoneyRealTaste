package com.albert.realmoneyrealtaste.adapter.webapi.follow

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.dto.UnfollowRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowTerminator
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 * 팔로우 삭제 API
 */
@RestController
class FollowDeleteApi(
    private val followTerminator: FollowTerminator,
) {

    /**
     * 사용자 언팔로우
     */
    @DeleteMapping("/api/members/{targetId}/follow")
    fun unfollow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
    ): ResponseEntity<Map<String, Any>> {
        followTerminator.unfollow(
            UnfollowRequest(
                followerId = principal.memberId,
                followingId = targetId,
            )
        )

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "언팔로우가 성공적으로 완료되었습니다."
            )
        )
    }
}
