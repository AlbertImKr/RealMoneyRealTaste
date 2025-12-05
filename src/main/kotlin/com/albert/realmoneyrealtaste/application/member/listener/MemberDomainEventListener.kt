package com.albert.realmoneyrealtaste.application.member.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberActivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberDeactivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberRegisteredDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.PasswordChangedDomainEvent
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
            com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowingsCount(event.fromMemberId, fromMemberCount)

        // 수신자의 팔로워 수 업데이트
        val toMemberCount = friendshipRepository.countFriends(
            event.toMemberId,
            com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus.ACCEPTED
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
            com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus.ACCEPTED
        )
        memberRepository.updateFollowingsCount(event.memberId, memberCount)

        // 친구 회원의 팔로워 수 업데이트
        val friendCount = friendshipRepository.countFriends(
            event.friendMemberId,
            com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus.ACCEPTED
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

    /**
     * 회원 활성화 도메인 이벤트 처리
     * - 활성화 완료 알림 이벤트 발행 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handleMemberActivated(event: MemberActivatedDomainEvent) {
        // TODO: 활성화 완료 알림 처리 (예: 웰컴 이메일, 통계 집계 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 비밀번호 변경 도메인 이벤트 처리
     * - 비밀번호 변경 알림 이벤트 발행 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handlePasswordChanged(event: PasswordChangedDomainEvent) {
        // TODO: 비밀번호 변경 알림 처리 (예: 보안 알림 이메일 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 회원 프로필 업데이트 도메인 이벤트 처리
     * - 프로필 변경 로깅 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handleMemberProfileUpdated(event: MemberProfileUpdatedDomainEvent) {
        // TODO: 프로필 업데이트 처리 (예: 분석 데이터 수집, 감사 로그 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 회원 비활성화 도메인 이벤트 처리
     * - 탈퇴 처리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handleMemberDeactivated(event: MemberDeactivatedDomainEvent) {
        // TODO: 회원 비활성화 처리 (예: 데이터 익명화, 구독 취소, 정리 작업 등)
        // 현재는 별도 처리 없음
    }
}
