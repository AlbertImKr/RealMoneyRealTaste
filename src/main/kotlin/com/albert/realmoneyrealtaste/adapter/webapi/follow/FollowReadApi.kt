package com.albert.realmoneyrealtaste.adapter.webapi.follow

import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FollowReadApi(
    private val followReader: FollowReader,
) {

    @GetMapping("/api/members/{memberId}/follow-stats")
    fun getFollowStats(
        @PathVariable memberId: Long,
    ): ResponseEntity<FollowStatsResponse> {
        val followStats = followReader.getFollowStats(memberId)
        return ResponseEntity(followStats, HttpStatus.OK)
    }
}
