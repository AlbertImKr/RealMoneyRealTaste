package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.domain.comment.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CommentReader {

    /**
     * 게시글의 댓글들을 조회합니다.
     */
    fun getComments(postId: Long, pageable: Pageable): Page<Comment>

    /**
     * 댓글의 대댓글들을 조회합니다.
     */
    fun getReplies(commentId: Long, pageable: Pageable): Page<Comment>

    /**
     * 게시글의 댓글 개수를 조회합니다.
     */
    fun getCommentCount(postId: Long): Long

    /**
     * 댓글 ID로 댓글을 조회합니다.
     */
    fun findById(commentId: Long): Comment
}
