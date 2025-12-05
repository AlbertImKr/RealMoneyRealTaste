package com.albert.realmoneyrealtaste.application.friend.listener

import com.albert.realmoneyrealtaste.application.event.MemberEventService
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Friendship 도메인 이벤트를 처리하는 리스너
 */
@Component
class FriendshipDomainEventListener(
    private val friendshipRepository: FriendshipRepository,
    private val memberEventService: MemberEventService,
) {

    /**
     * 회원 프로필 업데이트 도메인 이벤트 처리 (크로스 도메인)
     * - FriendRelationship의 nickname과 imageId 동기화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberProfileUpdated(event: MemberProfileUpdatedDomainEvent) {
        // 해당 회원과 관련된 모든 활성 친구 관계 업데이트
        val friendships = friendshipRepository.findAllActiveByMemberId(event.memberId)

        friendships.forEach { friendship ->
            friendship.updateMemberInfo(
                memberId = event.memberId,
                nickname = event.nickname,
                imageId = event.imageId
            )
            friendshipRepository.save(friendship)
        }
    }

    /**
     * 친구 요청 전송 도메인 이벤트 처리
     * - 친구 요청 알림 발송 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestSent(event: FriendRequestSentEvent) {
        // 친구 요청 이벤트 저장
        memberEventService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 요청을 보냈습니다",
            message = "${event.toMemberId}님에게 친구 요청을 보냈습니다.",
            relatedMemberId = event.toMemberId
        )

        memberEventService.createEvent(
            memberId = event.toMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
            title = "친구 요청을 받았습니다",
            message = "${event.fromMemberId}님에게서 친구 요청을 받았습니다.",
            relatedMemberId = event.fromMemberId
        )
    }

    /**
     * 친구 요청 수락 도메인 이벤트 처리
     * - 친구 관계 형성 알림 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestAccepted(event: FriendRequestAcceptedEvent) {
        // 친구 요청 수락 이벤트 저장
        memberEventService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_ACCEPTED,
            title = "친구 요청이 수락되었습니다",
            message = "${event.toMemberId}님이 친구 요청을 수락했습니다.",
            relatedMemberId = event.toMemberId
        )
    }

    /**
     * 친구 요청 거절 도메인 이벤트 처리
     * - 요청 거절 알림 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestRejected(event: FriendRequestRejectedEvent) {
        // 친구 요청 거절 이벤트 저장
        memberEventService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_REJECTED,
            title = "친구 요청이 거절되었습니다",
            message = "${event.toMemberId}님이 친구 요청을 거절했습니다.",
            relatedMemberId = event.toMemberId
        )
    }

    /**
     * 친구 관계 해제 도메인 이벤트 처리
     * - 친구 관계 종료 처리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendshipTerminated(event: FriendshipTerminatedEvent) {
        // 친구 관계 해제 이벤트 저장
        memberEventService.createEvent(
            memberId = event.memberId,
            eventType = MemberEventType.FRIENDSHIP_TERMINATED,
            title = "친구 관계가 해제되었습니다",
            message = "${event.friendMemberId}님과의 친구 관계가 해제되었습니다.",
            relatedMemberId = event.friendMemberId
        )
    }
}
