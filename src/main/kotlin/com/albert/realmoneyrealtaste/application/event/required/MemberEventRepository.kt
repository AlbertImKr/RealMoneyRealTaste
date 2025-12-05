package com.albert.realmoneyrealtaste.application.event.required

import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 회원 이벤트 저장소
 */
@Repository
interface MemberEventRepository : JpaRepository<MemberEvent, Long> {

    /**
     * 특정 회원의 이벤트를 페이징하여 조회 (최신순)
     */
    fun findByMemberIdOrderByCreatedAtDesc(
        memberId: Long,
        pageable: Pageable,
    ): Page<MemberEvent>

    /**
     * 특정 회원의 읽지 않은 이벤트 수를 조회
     */
    fun countByMemberIdAndIsReadFalse(memberId: Long): Long

    /**
     * 특정 회원의 특정 타입 이벤트를 페이징하여 조회
     */
    fun findByMemberIdAndEventTypeOrderByCreatedAtDesc(
        memberId: Long,
        eventType: MemberEventType,
        pageable: Pageable,
    ): Page<MemberEvent>

    /**
     * 특정 회원의 모든 이벤트를 읽음으로 표시
     */
    @Modifying
    @Query("UPDATE MemberEvent e SET e.isRead = true WHERE e.memberId = :memberId AND e.isRead = false")
    fun markAllAsRead(@Param("memberId") memberId: Long): Int

    /**
     * 회원 ID와 이벤트 ID로 이벤트 조회 (보안용)
     */
    fun findByIdAndMemberId(eventId: Long, memberId: Long): MemberEvent?

    /**
     * 특정 회원의 오래된 이벤트 삭제 (보관 기간 정책용)
     */
    @Modifying
    @Query("DELETE FROM MemberEvent e WHERE e.memberId = :memberId AND e.createdAt < :beforeDate")
    fun deleteOldEvents(
        @Param("memberId") memberId: Long,
        @Param("beforeDate") beforeDate: java.time.LocalDateTime,
    ): Int

    /**
     * 특정 회원의 특정 관련 게시물 이벤트 삭제 (게시물 삭제 시 연관 이벤트 정리)
     */
    @Modifying
    @Query("DELETE FROM MemberEvent e WHERE e.memberId = :memberId AND e.relatedPostId = :postId")
    fun deleteByMemberIdAndRelatedPostId(
        @Param("memberId") memberId: Long,
        @Param("postId") postId: Long,
    ): Int

    /**
     * 특정 회원의 특정 관련 댓글 이벤트 삭제 (댓글 삭제 시 연관 이벤트 정리)
     */
    @Modifying
    @Query("DELETE FROM MemberEvent e WHERE e.memberId = :memberId AND e.relatedCommentId = :commentId")
    fun deleteByMemberIdAndRelatedCommentId(
        @Param("memberId") memberId: Long,
        @Param("commentId") commentId: Long,
    ): Int
}
