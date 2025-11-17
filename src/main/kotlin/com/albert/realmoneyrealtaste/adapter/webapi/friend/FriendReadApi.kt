package com.albert.realmoneyrealtaste.adapter.webapi.friend

import com.albert.realmoneyrealtaste.application.friend.dto.FriendshipResponse
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FriendReadApi(
    private val friendshipReader: FriendshipReader,
) {

    @GetMapping("/api/members/{memberId}/friends")
    fun getFriends(
        @PathVariable memberId: Long,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<FriendshipResponse>?> {
        val friends = friendshipReader.findFriendsByMemberId(memberId, pageable)
        return ResponseEntity.ok(friends)
    }

    @GetMapping("/api/members/{memberId}/friends/count")
    fun getFriendsCount(
        @PathVariable memberId: Long,
    ): ResponseEntity<Long> {
        val count = friendshipReader.countFriendsByMemberId(memberId)
        return ResponseEntity.ok(count)
    }
}
