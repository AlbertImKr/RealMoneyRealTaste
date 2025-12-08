package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 회원 이벤트 조회 기능을 제공하는 인터페이스
 */
interface MemberEventReader {

    /**
     * 회원의 이벤트 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 이벤트 목록
     */
    fun readMemberEvents(
        memberId: Long,
        pageable: Pageable,
    ): Page<MemberEventResponse>

    /**
     * 회원의 특정 타입 이벤트 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param eventType 이벤트 타입
     * @param pageable 페이징 정보
     * @return 페이징된 이벤트 목록
     */
    fun readMemberEventsByType(
        memberId: Long,
        eventType: MemberEventType,
        pageable: Pageable,
    ): Page<MemberEventResponse>

    /**
     * 읽지 않은 이벤트 수를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 이벤트 수
     */
    fun readUnreadEventCount(memberId: Long): Long

    fun findByIdAndMemberId(eventId: Long, memberId: Long): MemberEvent
}
