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
            postId,
            CommentStatus.PUBLISHED,
            pageable
        )
    }

    override fun getReplies(commentId: Long, pageable: Pageable): Page<Comment> {
        return commentRepository.findByParentCommentIdAndStatus(
            commentId,
            CommentStatus.PUBLISHED,
            pageable
        )
    }

    override fun getCommentCount(postId: Long): Long {
        return commentRepository.countByPostIdAndStatus(
            postId,
            CommentStatus.PUBLISHED
        )
    }
}
