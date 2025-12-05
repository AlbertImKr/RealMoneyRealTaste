package com.albert.realmoneyrealtaste.application.event.listener

import com.albert.realmoneyrealtaste.application.event.MemberEventCreationService
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberActivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberDeactivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 모든 도메인 이벤트를 수신하여 회원 이벤트를 저장하는 전용 리스너
 */
@Component
class MemberEventDomainEventListener(
    private val memberEventCreationService: MemberEventCreationService,
) {

    // ===== 친구 관련 이벤트 =====

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestSent(event: FriendRequestSentEvent) {
        // 요청자에게 이벤트 저장
        memberEventCreationService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 요청을 보냈습니다",
            message = "${event.toMemberId}님에게 친구 요청을 보냈습니다.",
            relatedMemberId = event.toMemberId
        )

        // 수신자에게 이벤트 저장
        memberEventCreationService.createEvent(
            memberId = event.toMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
            title = "친구 요청을 받았습니다",
            message = "${event.fromMemberId}님이 친구 요청을 보냈습니다.",
            relatedMemberId = event.fromMemberId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestAccepted(event: FriendRequestAcceptedEvent) {
        // 요청자에게 이벤트 저장
        memberEventCreationService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_ACCEPTED,
            title = "친구 요청이 수락되었습니다",
            message = "${event.toMemberId}님이 친구 요청을 수락했습니다.",
            relatedMemberId = event.toMemberId
        )

        // 수락자에게 이벤트 저장
        memberEventCreationService.createEvent(
            memberId = event.toMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_ACCEPTED,
            title = "친구 요청을 수락했습니다",
            message = "${event.fromMemberId}님의 친구 요청을 수락했습니다.",
            relatedMemberId = event.fromMemberId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendRequestRejected(event: FriendRequestRejectedEvent) {
        memberEventCreationService.createEvent(
            memberId = event.fromMemberId,
            eventType = MemberEventType.FRIEND_REQUEST_REJECTED,
            title = "친구 요청이 거절되었습니다",
            message = "${event.toMemberId}님이 친구 요청을 거절했습니다.",
            relatedMemberId = event.toMemberId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleFriendshipTerminated(event: FriendshipTerminatedEvent) {
        memberEventCreationService.createEvent(
            memberId = event.memberId,
            eventType = MemberEventType.FRIENDSHIP_TERMINATED,
            title = "친구 관계가 해제되었습니다",
            message = "${event.friendMemberId}님과의 친구 관계가 해제되었습니다.",
            relatedMemberId = event.friendMemberId
        )
    }

    // ===== 게시물 관련 이벤트 =====

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostCreated(event: PostCreatedEvent) {
        memberEventCreationService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.POST_CREATED,
            title = "새 게시물을 작성했습니다",
            message = "새로운 맛집 게시물을 작성했습니다.",
            relatedPostId = event.postId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostDeleted(event: PostDeletedEvent) {
        memberEventCreationService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.POST_DELETED,
            title = "게시물을 삭제했습니다",
            message = "게시물을 삭제했습니다.",
            relatedPostId = event.postId
        )
    }

    // ===== 댓글 관련 이벤트 =====

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        // 댓글 작성자에게 이벤트 저장
        memberEventCreationService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.COMMENT_CREATED,
            title = "댓글을 작성했습니다",
            message = "댓글을 작성했습니다.",
            relatedPostId = event.postId,
            relatedCommentId = event.commentId
        )

        // 대댓글인 경우 부모 댓글 작성자에게 알림
        event.parentCommentAuthorId?.let { parentAuthorId ->
            if (parentAuthorId != event.authorMemberId) {
                memberEventCreationService.createEvent(
                    memberId = parentAuthorId,
                    eventType = MemberEventType.COMMENT_REPLIED,
                    title = "대댓글이 달렸습니다",
                    message = "댓글에 대댓글이 달렸습니다.",
                    relatedPostId = event.postId,
                    relatedCommentId = event.parentCommentId
                )
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentDeleted(event: CommentDeletedEvent) {
        memberEventCreationService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.COMMENT_DELETED,
            title = "댓글을 삭제했습니다",
            message = "댓글을 삭제했습니다.",
            relatedPostId = event.postId,
            relatedCommentId = event.commentId
        )
    }

    // ===== 회원 관련 이벤트 =====

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberActivated(event: MemberActivatedDomainEvent) {
        memberEventCreationService.createEvent(
            memberId = event.memberId,
            eventType = MemberEventType.ACCOUNT_ACTIVATED,
            title = "계정이 활성화되었습니다",
            message = "회원님의 계정이 성공적으로 활성화되었습니다."
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberDeactivated(event: MemberDeactivatedDomainEvent) {
        memberEventCreationService.createEvent(
            memberId = event.memberId,
            eventType = MemberEventType.ACCOUNT_DEACTIVATED,
            title = "계정이 비활성화되었습니다",
            message = "회원님의 계정이 비활성화되었습니다."
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberProfileUpdated(event: MemberProfileUpdatedDomainEvent) {
        memberEventCreationService.createEvent(
            memberId = event.memberId,
            eventType = MemberEventType.PROFILE_UPDATED,
            title = "프로필이 업데이트되었습니다",
            message = "회원님의 프로필 정보가 업데이트되었습니다."
        )
    }
}
