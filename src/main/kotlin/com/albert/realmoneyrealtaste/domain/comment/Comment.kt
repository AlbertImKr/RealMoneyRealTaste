package com.albert.realmoneyrealtaste.domain.comment

import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentStatusException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidParentCommentException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.UnauthorizedCommentOperationException
import com.albert.realmoneyrealtaste.domain.comment.value.CommentAuthor
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comment_post_id", columnList = "post_id"),
        Index(name = "idx_comment_author_id", columnList = "author_member_id"),
        Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
        Index(name = "idx_comment_status", columnList = "status"),
        Index(name = "idx_comment_created_at", columnList = "created_at")
    ]
)
class Comment protected constructor(
    postId: Long,
    author: CommentAuthor,
    content: CommentContent,
    parentCommentId: Long?,
    status: CommentStatus,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "post_id", nullable = false)
    var postId: Long = postId
        protected set

    @Embedded
    var author: CommentAuthor = author
        protected set

    @Embedded
    var content: CommentContent = content
        protected set

    @Column(name = "parent_comment_id")
    var parentCommentId: Long? = parentCommentId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus = status
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    /**
     * 댓글 내용을 수정합니다.
     *
     * @param memberId 요청한 회원 ID
     * @param content 새로운 댓글 내용
     * @throws InvalidCommentStatusException 댓글이 공개 상태가 아닌 경우
     * @throws UnauthorizedCommentOperationException 수정 권한이 없는 경우
     */
    fun update(memberId: Long, content: CommentContent) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 댓글을 삭제합니다 (Soft Delete).
     *
     * @param memberId 요청한 회원 ID
     * @throws InvalidCommentStatusException 댓글이 공개 상태가 아닌 경우
     * @throws UnauthorizedCommentOperationException 삭제 권한이 없는 경우
     */
    fun delete(memberId: Long) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.status = CommentStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 회원이 이 댓글을 수정할 권한이 있는지 확인합니다.
     *
     * @param memberId 회원 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    fun canEditBy(memberId: Long): Boolean {
        return author.memberId == memberId
    }

    /**
     * 회원이 이 댓글을 수정할 수 없다면 예외를 발생시킵니다.
     *
     * @param memberId 회원 ID
     * @throws UnauthorizedCommentOperationException 권한이 없는 경우
     */
    fun ensureCanEditBy(memberId: Long) {
        if (!canEditBy(memberId)) {
            throw UnauthorizedCommentOperationException("댓글을 수정할 권한이 없습니다.")
        }
    }

    /**
     * 댓글이 공개 상태인지 확인합니다.
     *
     * @throws InvalidCommentStatusException 공개 상태가 아닌 경우
     */
    fun ensurePublished() {
        if (status != CommentStatus.PUBLISHED) {
            throw InvalidCommentStatusException("댓글이 공개 상태가 아닙니다: $status")
        }
    }

    /**
     * 대댓글인지 확인합니다.
     */
    fun isReply(): Boolean = parentCommentId != null

    /**
     * 삭제된 댓글인지 확인합니다.
     */
    fun isDeleted(): Boolean = status == CommentStatus.DELETED

    companion object {
        /**
         * 새 댓글을 생성합니다.
         *
         * @param postId 게시글 ID
         * @param authorMemberId 작성자 회원 ID
         * @param authorNickname 작성자 닉네임
         * @param content 댓글 내용
         * @param parentCommentId 부모 댓글 ID (대댓글인 경우)
         * @return 생성된 댓글
         */
        fun create(
            postId: Long,
            authorMemberId: Long,
            authorNickname: String,
            content: CommentContent,
            parentCommentId: Long? = null,
        ): Comment {
            require(postId > 0) { "게시글 ID는 양수여야 합니다: $postId" }

            // 대댓글의 경우 부모 댓글 ID 검증
            if (parentCommentId != null && parentCommentId <= 0) {
                throw InvalidParentCommentException("부모 댓글 ID는 정수여야 합니다: $parentCommentId")
            }

            return Comment(
                postId = postId,
                author = CommentAuthor(authorMemberId, authorNickname),
                content = content,
                parentCommentId = parentCommentId,
                status = CommentStatus.PUBLISHED,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
}
