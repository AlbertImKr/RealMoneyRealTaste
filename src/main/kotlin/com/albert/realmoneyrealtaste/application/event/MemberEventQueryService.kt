package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.dto.MemberEventResponse
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventReader
import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
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
}
