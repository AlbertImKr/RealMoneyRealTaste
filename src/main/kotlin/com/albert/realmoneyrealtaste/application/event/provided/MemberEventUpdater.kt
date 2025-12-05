package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import java.time.LocalDateTime

/**
 * 회원 이벤트 수정 기능을 제공하는 인터페이스
 */
interface MemberEventUpdater {

    /**
     * 특정 회원의 모든 이벤트를 읽음으로 표시합니다.
     *
     * @param memberId 회원 ID
     * @return 읽음으로 표시된 이벤트 수
     */
    fun markAllAsRead(memberId: Long): Int

    /**
     * 특정 이벤트를 읽음으로 표시합니다.
     *
     * @param eventId 이벤트 ID
     * @param memberId 회원 ID (소유권 검증용)
     * @return 읽음으로 표시된 이벤트
     * @throws IllegalArgumentException 이벤트를 찾을 수 없거나 접근 권한이 없는 경우
     */
    fun markAsRead(eventId: Long, memberId: Long): MemberEvent

    /**
     * 오래된 이벤트를 삭제합니다.
     *
     * @param memberId 회원 ID
     * @param beforeDate 이 날짜보다 이전의 이벤트만 삭제
     * @return 삭제된 이벤트 수
     */
    fun deleteOldEvents(memberId: Long, beforeDate: LocalDateTime): Int
}
