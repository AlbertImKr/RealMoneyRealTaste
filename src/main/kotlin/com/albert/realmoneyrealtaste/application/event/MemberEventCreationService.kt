package com.albert.realmoneyrealtaste.application.event

import com.albert.realmoneyrealtaste.application.event.provided.MemberEventCreator
import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 이벤트 생성 서비스
 */
@Service
@Transactional
class MemberEventCreationService(
    private val memberEventRepository: MemberEventRepository,
) : MemberEventCreator {

    override fun createEvent(
        memberId: Long,
        eventType: MemberEventType,
        title: String,
        message: String,
        relatedMemberId: Long?,
        relatedPostId: Long?,
        relatedCommentId: Long?,
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
}
