package com.albert.realmoneyrealtaste.application.comment.required

import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.CommentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.Repository
import java.util.Optional

/**
 * 댓글 저장소 인터페이스
 */
interface CommentRepository : Repository<Comment, Long> {

    /**
     * 댓글을 저장합니다.
     *
     * @param comment 저장할 댓글
     * @return 저장된 댓글
     */
    fun save(comment: Comment): Comment

    /**
     * ID로 댓글을 조회합니다.
     *
     * @param id 댓글 ID
     * @return 댓글 Optional
     */
    fun findById(id: Long): Optional<Comment>

    /**
     * 게시글 ID로 댓글 목록을 조회합니다.
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    fun findByPostIdAndStatus(postId: Long, status: CommentStatus, pageable: Pageable): Page<Comment>

    fun findByPostIdAndParentCommentIdIsNullAndStatus(
        postId: Long,
        status: CommentStatus,
        pageable: Pageable,
    ): Page<Comment>

    /**
     * 부모 댓글 ID로 대댓글 목록을 조회합니다.
     *
     * @param parentCommentId 부모 댓글 ID
     * @param status 댓글 상태
     * @param pageable 페이징 정보
     * @return 대댓글 페이지
     */
    fun findByParentCommentIdAndStatus(parentCommentId: Long, status: CommentStatus, pageable: Pageable): Page<Comment>

    /**
     * 게시글의 댓글 수를 조회합니다.
     *
     * @param postId 게시글 ID
     * @param status 댓글 상태
     * @return 댓글 수
     */
    fun countByPostIdAndStatus(postId: Long, status: CommentStatus): Long

    /**
     * 회원이 작성한 댓글 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param status 댓글 상태
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    fun findByAuthorMemberIdAndStatus(memberId: Long, status: CommentStatus, pageable: Pageable): Page<Comment>

    /**
     * 댓글이 존재하는지 확인합니다.
     *
     * @param id 댓글 ID
     * @return 존재 여부
     */
    fun existsById(id: Long): Boolean

    /**
     * 댓글을 삭제합니다 (물리적 삭제).
     *
     * @param comment 삭제할 댓글
     */
    fun delete(comment: Comment)
}
