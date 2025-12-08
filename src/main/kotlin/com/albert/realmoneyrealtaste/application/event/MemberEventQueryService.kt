package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.application.event.exception.MemberEventNotFoundException
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventReader
import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
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
    private val memberEventRepository: MemberEventRepository,
) : MemberEventReader {

    override fun readMemberEvents(
        memberId: Long,
        pageable: Pageable,
    ): Page<MemberEventResponse> {
        val events = memberEventRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
        return events.map { MemberEventResponse.from(it) }
    }

    override fun readMemberEventsByType(
        memberId: Long,
        eventType: MemberEventType,
        pageable: Pageable,
    ): Page<MemberEventResponse> {
        val events = memberEventRepository.findByMemberIdAndEventTypeOrderByCreatedAtDesc(
            memberId, eventType, pageable
        )
        return events.map { MemberEventResponse.from(it) }
    }

    override fun readUnreadEventCount(memberId: Long): Long {
        return memberEventRepository.countByMemberIdAndIsReadFalse(memberId)
    }

    override fun findByIdAndMemberId(
        eventId: Long,
        memberId: Long,
    ): MemberEvent {
        try {
            return memberEventRepository.findByIdAndMemberId(eventId, memberId)
                ?: throw IllegalArgumentException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: $eventId")
        } catch (e: IllegalArgumentException) {
            throw MemberEventNotFoundException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: $eventId")
        }
    }
}
