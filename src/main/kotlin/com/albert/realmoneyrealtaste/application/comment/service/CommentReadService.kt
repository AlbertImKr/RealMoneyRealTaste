package com.albert.realmoneyrealtaste.application.comment.service

import com.albert.realmoneyrealtaste.application.comment.provided.CommentReader
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.CommentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CommentReadService(
    private val commentRepository: CommentRepository,
) : CommentReader {

    override fun getComments(postId: Long, pageable: Pageable): Page<Comment> {
        return commentRepository.findByPostIdAndStatus(
            postId = postId,
            status = CommentStatus.PUBLISHED,
            pageable = pageable
        )
    }

    override fun getReplies(commentId: Long, pageable: Pageable): Page<Comment> {
        return commentRepository.findByParentCommentIdAndStatus(
            parentCommentId = commentId,
            status = CommentStatus.PUBLISHED,
            pageable = pageable
        )
    }

    override fun getCommentCount(postId: Long): Long {
        return commentRepository.countByPostIdAndStatus(
            postId = postId,
            status = CommentStatus.PUBLISHED
        )
    }

    override fun findById(commentId: Long): Comment {
        return commentRepository.findById(commentId)
            .orElseThrow { IllegalArgumentException("댓글을 찾을 수 없습니다: $commentId") }
    }
}
