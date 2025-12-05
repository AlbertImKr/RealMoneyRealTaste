package com.albert.realmoneyrealtaste.domain.event

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 회원 이벤트 엔티티
 * 회원의 활동 기록을 저장하는 엔티티
 */
@Entity
@Table(
    name = "member_event",
    indexes = [
        Index(name = "idx_member_event_member_id", columnList = "member_id"),
        Index(name = "idx_member_event_created_at", columnList = "created_at"),
        Index(name = "idx_member_event_is_read", columnList = "is_read")
    ]
)
class MemberEvent protected constructor(
    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    val eventType: MemberEventType,

    @Column(name = "title", nullable = false, length = 100)
    val title: String,

    @Column(name = "message", nullable = false, length = 500)
    val message: String,

    @Column(name = "related_member_id")
    val relatedMemberId: Long? = null,

    @Column(name = "related_post_id")
    val relatedPostId: Long? = null,

    @Column(name = "related_comment_id")
    val relatedCommentId: Long? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity() {

    companion object {
        const val ERROR_MEMBER_ID_MUST_BE_POSITIVE = "회원 ID는 양수여야 합니다"
        const val ERROR_TITLE_MUST_NOT_BE_EMPTY = "제목은 비어있을 수 없습니다"
        const val ERROR_MESSAGE_MUST_NOT_BE_EMPTY = "메시지는 비어있을 수 없습니다"

        /**
         * 회원 이벤트 생성
         */
        fun create(
            memberId: Long,
            eventType: MemberEventType,
            title: String,
            message: String,
            relatedMemberId: Long? = null,
            relatedPostId: Long? = null,
            relatedCommentId: Long? = null,
        ): MemberEvent {
            require(memberId > 0) { ERROR_MEMBER_ID_MUST_BE_POSITIVE }
            require(title.isNotBlank()) { ERROR_TITLE_MUST_NOT_BE_EMPTY }
            require(message.isNotBlank()) { ERROR_MESSAGE_MUST_NOT_BE_EMPTY }

            return MemberEvent(
                memberId = memberId,
                eventType = eventType,
                title = title,
                message = message,
                relatedMemberId = relatedMemberId,
                relatedPostId = relatedPostId,
                relatedCommentId = relatedCommentId,
                isRead = false
            )
        }
    }

    /**
     * 이벤트를 읽음으로 표시
     */
    fun markAsRead() {
        isRead = true
    }

    /**
     * 이벤트를 모두 읽음으로 표시
     */
    fun markAllAsRead() {
        isRead = true
    }
}
