package com.albert.realmoneyrealtaste.application.comment.service

import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentNotFoundException
import com.albert.realmoneyrealtaste.application.comment.provided.CommentUpdater
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class CommentUpdateService(
    private val commentRepository: CommentRepository,
    private val memberReader: MemberReader,
) : CommentUpdater {

    override fun updateComment(request: CommentUpdateRequest): Comment {
        // 회원 존재 확인
        memberReader.readMemberById(request.memberId)

        // 댓글 조회
        val comment = commentRepository.findById(request.commentId)
            .orElseThrow { CommentNotFoundException("댓글을 찾을 수 없습니다: ${request.commentId}") }

        if (comment.isDeleted()) {
            throw CommentNotFoundException("삭제된 댓글은 수정할 수 없습니다: ${request.commentId}")
        }

        // 댓글 수정 (권한 확인 포함)
        comment.update(
            memberId = request.memberId,
            content = CommentContent(request.content)
        )

        return commentRepository.save(comment)
    }
}
