package com.albert.realmoneyrealtaste.application.event

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
) : MemberEventUpdater {

    override fun markAllAsRead(memberId: Long): Int {
        return memberEventRepository.markAllAsRead(memberId)
    }

    override fun markAsRead(eventId: Long, memberId: Long): MemberEvent {
        val event = memberEventRepository.findByIdAndMemberId(eventId, memberId)
            ?: throw IllegalArgumentException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: $eventId")

        event.markAsRead()
        return memberEventRepository.save(event)
    }

    override fun deleteOldEvents(memberId: Long, beforeDate: LocalDateTime): Int {
        return memberEventRepository.deleteOldEvents(memberId, beforeDate)
    }

    fun deletePostEvents(memberId: Long, postId: Long): Int {
        return memberEventRepository.deleteByMemberIdAndRelatedPostId(memberId, postId)
    }

    fun deleteCommentEvents(memberId: Long, commentId: Long): Int {
        return memberEventRepository.deleteByMemberIdAndRelatedCommentId(memberId, commentId)
    }
}
