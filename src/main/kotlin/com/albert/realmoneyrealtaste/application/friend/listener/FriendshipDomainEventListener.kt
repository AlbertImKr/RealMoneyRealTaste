package com.albert.realmoneyrealtaste.application.friend.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
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
        // TODO: 친구 요청 알림 처리 (예: 푸시 알림, 이메일 알림 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 친구 요청 수락 도메인 이벤트 처리
     * - 친구 관계 형성 알림 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestAccepted(event: FriendRequestAcceptedEvent) {
        // TODO: 친구 요청 수락 처리 (예: 양방향 알림, 친구 목록 업데이트 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 친구 요청 거절 도메인 이벤트 처리
     * - 요청 거절 알림 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestRejected(event: FriendRequestRejectedEvent) {
        // TODO: 친구 요청 거절 처리 (예: 요청자에게 알림 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 친구 관계 해제 도메인 이벤트 처리
     * - 친구 관계 종료 처리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendshipTerminated(event: FriendshipTerminatedEvent) {
        // TODO: 친구 관계 해제 처리 (예: 양방향 알림, 공유 데이터 정리 등)
        // 현재는 별도 처리 없음
    }
}
