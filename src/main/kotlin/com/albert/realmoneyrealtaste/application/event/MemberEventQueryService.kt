package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 이벤트 조회 서비스
 */
@Service
@Transactional(readOnly = true)
class MemberEventQueryService(
    private val memberEventService: MemberEventService,
) {

    /**
     * 회원의 이벤트 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 이벤트 목록
     */
    fun getMemberEvents(
        memberId: Long,
        pageable: Pageable,
    ): Page<MemberEventResponse> {
        val events = memberEventService.getMemberEvents(memberId, pageable)
        return events.map { MemberEventResponse.from(it) }
    }

    /**
     * 회원의 특정 타입 이벤트 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param eventType 이벤트 타입
     * @param pageable 페이징 정보
     * @return 페이징된 이벤트 목록
     */
    fun getMemberEventsByType(
        memberId: Long,
        eventType: MemberEventType,
        pageable: Pageable,
    ): Page<MemberEventResponse> {
        val events = memberEventService.getMemberEventsByType(memberId, eventType, pageable)
        return events.map { MemberEventResponse.from(it) }
    }

    /**
     * 읽지 않은 이벤트 수를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 이벤트 수
     */
    fun getUnreadEventCount(memberId: Long): Long {
        return memberEventService.getUnreadEventCount(memberId)
    }
}
