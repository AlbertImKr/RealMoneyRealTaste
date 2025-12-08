package com.albert.realmoneyrealtaste.application.event.dto

import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import java.time.LocalDateTime

/**
 * 회원 이벤트 응답 DTO
 */
data class MemberEventResponse(
    val id: Long,
    val eventType: MemberEventType,
    val title: String,
    val message: String,
    val relatedMemberId: Long?,
    val relatedPostId: Long?,
    val relatedCommentId: Long?,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        /**
         * MemberEvent 엔티티로부터 응답 DTO 생성
         */
        fun from(event: com.albert.realmoneyrealtaste.domain.event.MemberEvent): MemberEventResponse {
            return MemberEventResponse(
                id = event.requireId(),
                eventType = event.eventType,
                title = event.title,
                message = event.message,
                relatedMemberId = event.relatedMemberId,
                relatedPostId = event.relatedPostId,
                relatedCommentId = event.relatedCommentId,
                isRead = event.isRead,
                createdAt = event.createdAt
            )
        }
    }
}
