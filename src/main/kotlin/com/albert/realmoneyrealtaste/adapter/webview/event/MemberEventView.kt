package com.albert.realmoneyrealtaste.adapter.webview.event

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventReader
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventUpdater
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * 회원 이벤트 뷰 컨트롤러
 * 회원의 이벤트 목록을 조회하는 웹 페이지를 제공합니다.
 */
@Controller
class MemberEventView(
    private val memberEventReader: MemberEventReader,
    private val memberEventUpdater: MemberEventUpdater,
) {

    /**
     * 이벤트 목록 프래그먼트
     */
    @GetMapping("/members/{memberId}/events/fragment")
    fun eventsFragment(
        @PathVariable memberId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC, size = 20) pageable: Pageable,
        @RequestParam(required = false) eventType: String?,
        model: Model,
    ): String {
        // 본인의 이벤트만 볼 수 있도록 체크
        if (principal.id != memberId) {
            return "redirect:/members/" + principal.id + "/events/fragment"
        }

        val events: Page<MemberEventResponse> = if (eventType.isNullOrBlank()) {
            memberEventReader.readMemberEvents(memberId, pageable)
        } else {
            try {
                memberEventReader.readMemberEventsByType(
                    memberId,
                    MemberEventType.valueOf(eventType.uppercase()),
                    pageable
                )
            } catch (e: IllegalArgumentException) {
                memberEventReader.readMemberEvents(memberId, pageable)
            }
        }

        model.addAttribute("member", principal)
        model.addAttribute("author", principal)
        model.addAttribute("events", events)
        model.addAttribute("page", events)
        model.addAttribute("eventType", eventType)

        return "event/fragments/events :: events"
    }
}
