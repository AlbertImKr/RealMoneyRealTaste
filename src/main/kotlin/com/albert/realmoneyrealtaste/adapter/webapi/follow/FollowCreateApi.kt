package com.albert.realmoneyrealtaste.adapter.webapi.follow

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 팔로우 생성 API
 */
@RestController
class FollowCreateApi(
    private val followCreator: FollowCreator,
) {

    /**
     * 사용자 팔로우
     */
    @PostMapping("/api/members/{targetId}/follow")
    fun follow(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable targetId: Long,
    ): ResponseEntity<Map<String, Any>> {
        followCreator.follow(
            FollowCreateRequest(
                followerId = principal.memberId,
                followingId = targetId,
            )
        )

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "팔로우가 성공적으로 완료되었습니다."
            )
        )
    }
}
