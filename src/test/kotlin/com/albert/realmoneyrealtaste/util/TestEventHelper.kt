package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.application.event.MemberEventCreationService
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TestEventHelper(
    private val memberEventCreationService: MemberEventCreationService,
    private val entityManager: EntityManager,
) {

    @Transactional
    fun createEvent(
        memberId: Long,
        eventType: MemberEventType,
        title: String,
        message: String,
        isRead: Boolean = false,
        relatedMemberId: Long? = null,
        relatedPostId: Long? = null,
        relatedCommentId: Long? = null,
    ): MemberEvent {
        val event = memberEventCreationService.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )
        return memberEventCreationService.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )
    }
}
