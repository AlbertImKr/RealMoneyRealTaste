package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.provided.MemberEventReader
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventUpdater
import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 회원 이벤트 수정 서비스
 */
@Service
@Transactional
class MemberEventUpdateService(
    private val memberEventRepository: MemberEventRepository,
    private val memberEventReader: MemberEventReader,
) : MemberEventUpdater {

    override fun markAllAsRead(memberId: Long): Int {
        return memberEventRepository.markAllAsRead(memberId)
    }

    override fun markAsRead(eventId: Long, memberId: Long): MemberEvent {
        val event = memberEventReader.findByIdAndMemberId(eventId, memberId)

        event.markAsRead()
        return memberEventRepository.save(event)
    }

    override fun deleteOldEvents(memberId: Long, beforeDate: LocalDateTime): Int {
        return memberEventRepository.deleteOldEvents(memberId, beforeDate)
    }
}
