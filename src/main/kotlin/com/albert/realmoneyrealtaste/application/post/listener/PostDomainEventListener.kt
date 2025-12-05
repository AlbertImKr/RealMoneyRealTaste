package com.albert.realmoneyrealtaste.application.post.listener

import com.albert.realmoneyrealtaste.application.event.MemberEventService
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
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
 * Post 도메인 이벤트를 처리하는 리스너
 */
@Component
class PostDomainEventListener(
    private val postRepository: com.albert.realmoneyrealtaste.application.post.required.PostRepository,
    private val memberEventService: MemberEventService,
) {

    /**
     * 회원 프로필 업데이트 도메인 이벤트 처리 (크로스 도메인)
     * - Post의 작성자 정보 동기화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberProfileUpdated(event: MemberProfileUpdatedDomainEvent) {
        // Post의 작성자 정보 업데이트
        postRepository.updateAuthorInfo(
            authorMemberId = event.memberId,
            nickname = event.nickname,
            imageId = event.imageId
        )
    }

    /**
     * 댓글 생성 도메인 이벤트 처리 (크로스 도메인)
     * - 포스트의 댓글 수 증가
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        // 포스트의 댓글 수 증가
        postRepository.incrementCommentCount(event.postId)

        // 댓글 작성자에게 이벤트 저장
        memberEventService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.COMMENT_CREATED,
            title = "댓글을 작성했습니다",
            message = "게시물에 댓글을 작성했습니다.",
            relatedPostId = event.postId,
            relatedCommentId = event.commentId
        )

        // TODO: 포스트 작성자에게 알림 (향후 확장)
    }

    /**
     * 댓글 삭제 도메인 이벤트 처리 (크로스 도메인)
     * - 포스트의 댓글 수 감소
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentDeleted(event: CommentDeletedEvent) {
        // 포스트의 댓글 수 감소
        postRepository.decrementCommentCount(event.postId)

        // 댓글 삭제 이벤트 저장
        memberEventService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.COMMENT_DELETED,
            title = "댓글을 삭제했습니다",
            message = "게시물의 댓글을 삭제했습니다.",
            relatedPostId = event.postId
        )
    }

    /**
     * 포스트 생성 도메인 이벤트 처리
     * - 포스트 생성 통계 집계 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostCreated(event: PostCreatedEvent) {
        // 포스트 생성 이벤트 저장
        memberEventService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.POST_CREATED,
            title = "새 게시물을 작성했습니다",
            message = "새로운 게시물을 작성했습니다.",
            relatedPostId = event.postId
        )
    }

    /**
     * 포스트 삭제 도메인 이벤트 처리
     * - 관련 데이터 정리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostDeleted(event: PostDeletedEvent) {
        // 포스트 삭제 이벤트 저장
        memberEventService.createEvent(
            memberId = event.authorMemberId,
            eventType = MemberEventType.POST_DELETED,
            title = "게시물을 삭제했습니다",
            message = "게시물을 삭제했습니다.",
            relatedPostId = event.postId
        )
    }
}
