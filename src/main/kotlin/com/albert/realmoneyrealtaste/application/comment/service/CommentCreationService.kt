package com.albert.realmoneyrealtaste.application.comment.service

import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreationException
import com.albert.realmoneyrealtaste.application.comment.provided.CommentCreator
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.application.post.provided.PostReader
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.CommentStatus
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.exceptions.CommentNotFoundException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentStatusException
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentCreationService(
    private val commentRepository: CommentRepository,
    private val memberReader: MemberReader,
    private val postReader: PostReader,
    private val eventPublisher: ApplicationEventPublisher,
) : CommentCreator {

    @Transactional
    override fun createComment(request: CommentCreateRequest): Comment {
        validateCommentCreation(request.memberId, request.postId)

        val nickname = memberReader.getNicknameById(request.memberId)

        // 댓글 생성
        val comment = Comment.create(
            postId = request.postId,
            authorMemberId = request.memberId,
            authorNickname = nickname,
            content = CommentContent(request.content)
        )

        // 저장
        val savedComment = commentRepository.save(comment)

        // 이벤트 발행
        eventPublisher.publishEvent(
            CommentCreatedEvent(
                commentId = savedComment.requireId(),
                postId = savedComment.postId,
                authorMemberId = savedComment.author.memberId,
                parentCommentId = null,
                createdAt = savedComment.createdAt
            )
        )

        return savedComment
    }

    @Transactional
    override fun createReply(request: ReplyCreateRequest): Comment {
        validateCommentCreation(request.memberId, request.postId)

        val nickname = memberReader.getNicknameById(request.memberId)

        // 부모 댓글 검증
        validateParentComment(request)

        // 대댓글 생성
        val reply = Comment.create(
            postId = request.postId,
            authorMemberId = request.memberId,
            authorNickname = nickname,
            content = CommentContent(request.content),
            parentCommentId = request.parentCommentId
        )

        // 저장
        val savedReply = commentRepository.save(reply)

        // 이벤트 발행
        eventPublisher.publishEvent(
            CommentCreatedEvent(
                commentId = savedReply.requireId(),
                postId = savedReply.postId,
                authorMemberId = savedReply.author.memberId,
                parentCommentId = savedReply.parentCommentId,
                createdAt = savedReply.createdAt
            )
        )

        return savedReply
    }

    /**
     * 댓글 작성 전 검증 로직
     * @param memberId 작성자 회원 ID
     * @param postId 댓글이 작성될 게시글 ID
     *
     * @throws CommentCreationException 유효하지 않은 회원이거나 게시글인 경우 발생
     */
    private fun validateCommentCreation(memberId: Long, postId: Long) {
        // Member 존재 및 상태 검증
        if (!memberReader.existsActiveMemberById(memberId)) {
            throw CommentCreationException("유효한 회원이 아닙니다: $memberId")
        }

        // Post 존재 및 상태 검증
        if (!postReader.existsPublishedPostById(postId)) {
            throw CommentCreationException("유효한 게시글이 아닙니다: $postId")
        }
    }

    /**
     * 부모 댓글 검증 로직
     * @param request 대댓글 작성 요청 DTO
     *
     * @throws CommentNotFoundException 부모 댓글이 존재하지 않는 경우 발생
     * @throws InvalidCommentStatusException 부모 댓글이 공개 상태가 아닌 경우 발생
     * @throws CommentCreationException 부모 댓글과 게시글이 다르거나, 부모 댓글이 대댓글인 경우 발생
     */
    private fun validateParentComment(request: ReplyCreateRequest) {
        val parentComment = commentRepository.findById(request.parentCommentId)
            .orElseThrow { CommentNotFoundException("부모 댓글을 찾을 수 없습니다: ${request.parentCommentId}") }

        if (parentComment.status != CommentStatus.PUBLISHED) {
            throw InvalidCommentStatusException("공개된 댓글에만 대댓글을 작성할 수 있습니다: ${parentComment.status}")
        }

        if (parentComment.postId != request.postId) {
            throw CommentCreationException("부모 댓글과 같은 게시글에만 대댓글을 작성할 수 있습니다.")
        }

        if (parentComment.isReply()) {
            throw CommentCreationException("대댓글에는 대댓글을 작성할 수 없습니다.")
        }
    }
}
