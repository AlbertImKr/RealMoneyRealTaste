package com.albert.realmoneyrealtaste.application.comment.listener

import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.application.event.MemberEventService
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentUpdatedEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Comment 도메인 이벤트를 처리하는 리스너
 */
@Component
class CommentDomainEventListener(
    private val commentRepository: CommentRepository,
    private val memberEventService: MemberEventService,
) {

    /**
     * 회원 프로필 업데이트 도메인 이벤트 처리 (크로스 도메인)
     * - Comment의 작성자 닉네임 동기화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleMemberProfileUpdated(event: MemberProfileUpdatedDomainEvent) {
        // Comment의 작성자 닉네임 업데이트
        event.nickname?.let { nickname ->
            commentRepository.updateAuthorNickname(
                authorMemberId = event.memberId,
                nickname = nickname
            )
        }
    }

    /**
     * 댓글 생성 도메인 이벤트 처리
     * - 대댓글인 경우 부모 댓글의 대댓글 수 증가
     * - 댓글 알림 발송 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        // 대댓글인 경우 부모 댓글의 대댓글 수 증가
        event.parentCommentId?.let { parentCommentId ->
            commentRepository.incrementRepliesCount(parentCommentId)

            if (event.parentCommentAuthorId != null) {
                // 대댓글 이벤트 저장 (부모 댓글 작성자에게)
                memberEventService.createEvent(
                    memberId = event.parentCommentAuthorId,
                    eventType = MemberEventType.COMMENT_REPLIED,
                    title = "대댓글이 달렸습니다",
                    message = "댓글에 대댓글이 달렸습니다.",
                    relatedPostId = event.postId,
                    relatedCommentId = event.parentCommentId
                )
            }
        }
    }

    /**
     * 댓글 수정 도메인 이벤트 처리
     * - 댓글 수정 로깅 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentUpdated(event: CommentUpdatedEvent) {
        // TODO: 댓글 수정 처리 (예: 수정 알림, 감사 로깅 등)
        // 현재는 별도 처리 없음
    }

    /**
     * 댓글 삭제 도메인 이벤트 처리
     * - 대댓글인 경우 부모 댓글의 대댓글 수 감소
     * - 관련 데이터 정리 (향후 확장용)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCommentDeleted(event: CommentDeletedEvent) {
        // 대댓글인 경우 부모 댓글의 대댓글 수 감소
        event.parentCommentId?.let { parentCommentId ->
            commentRepository.decrementRepliesCount(parentCommentId)
        }

        // TODO: 댓글 삭제 처리 (예: 대댓글 정리, 통계 업데이트 등)
        // 현재는 별도 처리 없음
    }
}
