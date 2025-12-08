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
        Index(name = "idx_member_event_event_type", columnList = "event_type"),
        Index(name = "idx_member_event_is_read", columnList = "is_read"),
        Index(name = "idx_member_event_created_at", columnList = "created_at")
    ]
)
class MemberEvent protected constructor(
    memberId: Long,

    eventType: MemberEventType,

    title: String,

    message: String,

    relatedMemberId: Long?,

    relatedPostId: Long?,

    relatedCommentId: Long?,

    isRead: Boolean,

    createdAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "member_id", nullable = false)
    var memberId: Long = memberId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    var eventType: MemberEventType = eventType
        protected set

    @Column(name = "title", nullable = false, length = 100)
    var title: String = title
        protected set

    @Column(name = "message", nullable = false, length = 500)
    var message: String = message
        protected set

    @Column(name = "related_member_id")
    var relatedMemberId: Long? = relatedMemberId
        protected set

    @Column(name = "related_post_id")
    var relatedPostId: Long? = relatedPostId
        protected set

    @Column(name = "related_comment_id")
    var relatedCommentId: Long? = relatedCommentId
        protected set

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = isRead
        protected set

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    init {
        validate()
    }

    private fun validate() {
        require(memberId > 0) { ERROR_MEMBER_ID_MUST_BE_POSITIVE }
        require(title.isNotBlank()) { ERROR_TITLE_MUST_NOT_BE_EMPTY }
        require(message.isNotBlank()) { ERROR_MESSAGE_MUST_NOT_BE_EMPTY }
    }

    /**
     * 이벤트를 읽음으로 표시
     */
    fun markAsRead() {
        isRead = true
    }

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
            return MemberEvent(
                memberId = memberId,
                eventType = eventType,
                title = title,
                message = message,
                relatedMemberId = relatedMemberId,
                relatedPostId = relatedPostId,
                relatedCommentId = relatedCommentId,
                isRead = false,
                createdAt = LocalDateTime.now(),
            )
        }
    }
}
