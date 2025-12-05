package com.albert.realmoneyrealtaste.adapter.webapi.event

import com.albert.realmoneyrealtaste.application.event.MemberEventQueryService
import com.albert.realmoneyrealtaste.application.event.MemberEventService
import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 회원 이벤트 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/events")
class MemberEventApi(
    private val memberEventQueryService: MemberEventQueryService,
    private val memberEventService: MemberEventService,
) {

    /**
     * 회원의 이벤트 목록을 조회합니다.
     */
    @GetMapping
    fun getMemberEvents(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<MemberEventResponse>> {
        val memberId = userDetails.username.toLong()
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val events = memberEventQueryService.getMemberEvents(memberId, pageable)

        return ResponseEntity.ok(events)
    }

    /**
     * 특정 타입의 회원 이벤트 목록을 조회합니다.
     */
    @GetMapping("/type/{eventType}")
    fun getMemberEventsByType(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable eventType: MemberEventType,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<MemberEventResponse>> {
        val memberId = userDetails.username.toLong()
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val events = memberEventQueryService.getMemberEventsByType(memberId, eventType, pageable)

        return ResponseEntity.ok(events)
    }

    /**
     * 읽지 않은 이벤트 수를 조회합니다.
     */
    @GetMapping("/unread-count")
    fun getUnreadEventCount(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Long> {
        val memberId = userDetails.username.toLong()
        val count = memberEventQueryService.getUnreadEventCount(memberId)

        return ResponseEntity.ok(count)
    }

    /**
     * 모든 이벤트를 읽음으로 표시합니다.
     */
    @PostMapping("/mark-all-read")
    fun markAllAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Int> {
        val memberId = userDetails.username.toLong()
        val count = memberEventService.markAllAsRead(memberId)

        return ResponseEntity.ok(count)
    }

    /**
     * 특정 이벤트를 읽음으로 표시합니다.
     */
    @PostMapping("/{eventId}/read")
    fun markAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable eventId: Long,
    ): ResponseEntity<MemberEventResponse> {
        val memberId = userDetails.username.toLong()
        val event = memberEventService.markAsRead(eventId, memberId)

        return ResponseEntity.ok(MemberEventResponse.from(event))
    }
}
