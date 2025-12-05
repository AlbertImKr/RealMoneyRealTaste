package com.albert.realmoneyrealtaste.application.member.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberRegisteredDomainEvent
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 도메인 이벤트를 처리하여 애플리케이션 이벤트를 발행하는 리스너
 */
@Component
class MemberDomainEventListener(
    private val activationTokenGenerator: ActivationTokenGenerator,
    private val eventPublisher: ApplicationEventPublisher,
    private val memberRepository: MemberRepository,
    private val friendshipRepository: FriendshipRepository,
) {

    /**
     * 포스트 생성 도메인 이벤트 처리 (크로스 도메인)
     * - 작성자의 게시글 수 증가
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostCreated(event: PostCreatedEvent) {
        memberRepository.incrementPostCount(event.authorMemberId)
    }

    /**
     * 포스트 삭제 도메인 이벤트 처리 (크로스 도메인)
     * - 작성자의 게시글 수 감소
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostDeleted(event: PostDeletedEvent) {
        memberRepository.decrementPostCount(event.authorMemberId)
    }

    /**
     * 친구 요청 수락 도메인 이벤트 처리 (크로스 도메인)
     * - 양방향 회원의 팔로워/팔로잉 수 업데이트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestAccepted(event: FriendRequestAcceptedEvent) {
        // 요청자의 팔로잉 수 업데이트
        val fromMemberCount = friendshipRepository.countFriends(
            event.fromMemberId,
            FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowingsCount(event.fromMemberId, fromMemberCount)

        // 수신자의 팔로워 수 업데이트
        val toMemberCount = friendshipRepository.countFriends(
            event.toMemberId,
            FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowersCount(event.toMemberId, toMemberCount)
    }

    /**
     * 친구 관계 해제 도메인 이벤트 처리 (크로스 도메인)
     * - 양방향 회원의 팔로워/팔로잉 수 감소
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendshipTerminated(event: FriendshipTerminatedEvent) {
        // 회원의 팔로잉 수 업데이트
        val memberCount = friendshipRepository.countFriends(
            event.memberId,
            FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowingsCount(event.memberId, memberCount)

        // 친구 회원의 팔로워 수 업데이트
        val friendCount = friendshipRepository.countFriends(
            event.friendMemberId,
            FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowersCount(event.friendMemberId, friendCount)
    }

    /**
     * 회원 등록 도메인 이벤트 처리
     * - 활성화 이메일 발송 요청 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handleMemberRegistered(event: MemberRegisteredDomainEvent) {
        val activationToken = activationTokenGenerator.generate(event.memberId)

        eventPublisher.publishEvent(
            EmailSendRequestedEvent.ActivationEmail(
                email = Email(event.email),
                nickname = Nickname(event.nickname),
                activationToken = activationToken
            )
        )
    }
}
