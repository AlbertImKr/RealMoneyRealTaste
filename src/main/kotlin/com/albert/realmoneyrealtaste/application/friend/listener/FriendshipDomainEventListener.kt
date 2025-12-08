package com.albert.realmoneyrealtaste.application.friend.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
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
}
