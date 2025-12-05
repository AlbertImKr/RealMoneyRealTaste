package com.albert.realmoneyrealtaste.application.post.listener

import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
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
    }

    /**
     * 포스트 생성 도메인 이벤트 처리
     * - 포스트 생성 통계 집계 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostCreated(event: PostCreatedEvent) {
        // TODO: 포스트 생성 처리 (예: 알림 발송, 통계 집계, 검색 인덱싱 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 포스트 삭제 도메인 이벤트 처리
     * - 관련 데이터 정리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handlePostDeleted(event: PostDeletedEvent) {
        // TODO: 포스트 삭제 처리 (예: 관련 댓글/하트 정리, 통계 업데이트 등)
        // 현재는 별도 처리 없음
    }
}
