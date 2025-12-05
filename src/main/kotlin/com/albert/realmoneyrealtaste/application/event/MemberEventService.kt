package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 회원 이벤트 서비스
 */
@Service
@Transactional
class MemberEventService(
    private val memberEventRepository: MemberEventRepository,
) {

    /**
     * 회원 이벤트 생성
     */
    fun createEvent(
        memberId: Long,
        eventType: MemberEventType,
        title: String,
        message: String,
        relatedMemberId: Long? = null,
        relatedPostId: Long? = null,
        relatedCommentId: Long? = null,
    ): MemberEvent {
        val event = MemberEvent.create(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )

        return memberEventRepository.save(event)
    }

    /**
     * 회원의 이벤트 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    fun getMemberEvents(
        memberId: Long,
        pageable: Pageable,
    ): Page<MemberEvent> {
        return memberEventRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
    }

    /**
     * 회원의 특정 타입 이벤트 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    fun getMemberEventsByType(
        memberId: Long,
        eventType: MemberEventType,
        pageable: Pageable,
    ): Page<MemberEvent> {
        return memberEventRepository.findByMemberIdAndEventTypeOrderByCreatedAtDesc(
            memberId, eventType, pageable
        )
    }

    /**
     * 읽지 않은 이벤트 수 조회
     */
    @Transactional(readOnly = true)
    fun getUnreadEventCount(memberId: Long): Long {
        return memberEventRepository.countByMemberIdAndIsReadFalse(memberId)
    }

    /**
     * 모든 이벤트를 읽음으로 표시
     */
    fun markAllAsRead(memberId: Long): Int {
        return memberEventRepository.markAllAsRead(memberId)
    }

    /**
     * 특정 이벤트를 읽음으로 표시
     */
    fun markAsRead(eventId: Long, memberId: Long): MemberEvent {
        val event = memberEventRepository.findByIdAndMemberId(eventId, memberId)
            ?: throw IllegalArgumentException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: $eventId")

        event.markAsRead()
        return memberEventRepository.save(event)
    }

    /**
     * 오래된 이벤트 삭제 (보관 정책)
     */
    fun deleteOldEvents(memberId: Long, beforeDate: LocalDateTime): Int {
        return memberEventRepository.deleteOldEvents(memberId, beforeDate)
    }

    /**
     * 관련 게시물 이벤트 삭제 (게시물 삭제 시 호출)
     */
    fun deletePostEvents(memberId: Long, postId: Long): Int {
        return memberEventRepository.deleteByMemberIdAndRelatedPostId(memberId, postId)
    }

    /**
     * 관련 댓글 이벤트 삭제 (댓글 삭제 시 호출)
     */
    fun deleteCommentEvents(memberId: Long, commentId: Long): Int {
        return memberEventRepository.deleteByMemberIdAndRelatedCommentId(memberId, commentId)
    }
}
