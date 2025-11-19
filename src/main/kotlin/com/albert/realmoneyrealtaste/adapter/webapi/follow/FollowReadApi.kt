package com.albert.realmoneyrealtaste.adapter.webapi.follow

import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 팔로우 조회 API
 */
@RestController
class FollowReadApi(
    private val followReader: FollowReader,
) {

    /**
     * 팔로우 통계 조회
     */
    @GetMapping("/api/members/{memberId}/follow-stats")
    fun getFollowStats(
        @PathVariable memberId: Long,
    ): ResponseEntity<FollowStatsResponse> {
        val followStats = followReader.getFollowStats(memberId)
        return ResponseEntity(followStats, HttpStatus.OK)
    }

    /**
     * 특정 사용자의 팔로잉 목록 조회
     */
    @GetMapping("/api/members/{memberId}/followings")
    fun getFollowings(
        @PathVariable memberId: Long,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Map<String, Any>> {
        val followings = if (keyword.isNullOrBlank()) {
            followReader.findFollowingsByMemberId(memberId, pageable)
        } else {
            followReader.searchFollowings(memberId, keyword, pageable)
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to followings,
                "message" to "팔로잉 목록 조회 성공"
            )
        )
    }

    /**
     * 특정 사용자의 팔로워 목록 조회
     */
    @GetMapping("/api/members/{memberId}/followers")
    fun getFollowers(
        @PathVariable memberId: Long,
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Map<String, Any>> {
        val followers = if (keyword.isNullOrBlank()) {
            followReader.findFollowersByMemberId(memberId, pageable)
        } else {
            followReader.searchFollowers(memberId, keyword, pageable)
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "data" to followers,
                "message" to "팔로워 목록 조회 성공"
            )
        )
    }
}
