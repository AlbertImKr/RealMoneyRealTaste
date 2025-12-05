package com.albert.realmoneyrealtaste.domain.comment

import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentUpdatedEvent
import com.albert.realmoneyrealtaste.domain.comment.value.CommentAuthor
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import com.albert.realmoneyrealtaste.domain.common.AggregateRoot
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Transient
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
    repliesCount: Long,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) : BaseEntity(), AggregateRoot {

    companion object {
        const val ERROR_POST_ID_MUST_BE_POSITIVE = "게시글 ID는 양수여야 합니다: %d"
        const val ERROR_PARENT_COMMENT_ID_MUST_BE_POSITIVE = "부모 댓글 ID는 양수여야 합니다: %d"
        const val ERROR_CANNOT_EDIT_COMMENT = "댓글을 수정할 권한이 없습니다."
        const val ERROR_COMMENT_NOT_PUBLISHED = "댓글이 공개 상태가 아닙니다: %s"

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
            require(postId > 0) { ERROR_POST_ID_MUST_BE_POSITIVE.format(postId) }

            require(parentCommentId == null || parentCommentId > 0) {
                ERROR_PARENT_COMMENT_ID_MUST_BE_POSITIVE.format(
                    parentCommentId
                )
            }

            val comment = Comment(
                postId = postId,
                author = CommentAuthor(authorMemberId, authorNickname),
                content = content,
                parentCommentId = parentCommentId,
                status = CommentStatus.PUBLISHED,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                repliesCount = 0,
            )

            // 도메인 이벤트 발행
            comment.addDomainEvent(
                CommentCreatedEvent(
                    commentId = 0L, // drainDomainEvents에서 실제 ID로 설정
                    postId = postId,
                    authorMemberId = authorMemberId,
                    parentCommentId = parentCommentId,
                    createdAt = comment.createdAt
                )
            )

            return comment
        }
    }

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

    @Column(name = "reply_count", nullable = false)
    var repliesCount: Long = repliesCount
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
     * @throws IllegalArgumentException 댓글이 공개 상태가 아닌 경우 혹은 수정 권한이 없는 경우
     */
    fun update(memberId: Long, content: CommentContent) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.content = content
        this.updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            CommentUpdatedEvent(
                commentId = requireId(),
                postId = postId,
                authorMemberId = author.memberId,
                updatedAt = updatedAt,
            )
        )
    }

    /**
     * 댓글을 삭제합니다 (Soft Delete).
     *
     * @param memberId 요청한 회원 ID
     * @throws IllegalArgumentException 댓글이 공개 상태가 아닌 경우 혹은 삭제 권한이 없는 경우
     */
    fun delete(memberId: Long) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.status = CommentStatus.DELETED
        this.updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            CommentDeletedEvent(
                commentId = requireId(),
                parentCommentId = parentCommentId,
                postId = postId,
                authorMemberId = author.memberId,
                deletedAt = updatedAt,
            )
        )
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
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    fun ensureCanEditBy(memberId: Long) {
        require(canEditBy(memberId)) { ERROR_CANNOT_EDIT_COMMENT }
    }

    /**
     * 댓글이 공개 상태인지 확인합니다.
     *
     * @throws IllegalArgumentException 공개 상태가 아닌 경우
     */
    fun ensurePublished() {
        require(status == CommentStatus.PUBLISHED) { ERROR_COMMENT_NOT_PUBLISHED.format(status.name) }
    }

    /**
     * 대댓글인지 확인합니다.
     */
    fun isReply(): Boolean = parentCommentId != null

    /**
     * 삭제된 댓글인지 확인합니다.
     */
    fun isDeleted(): Boolean = status == CommentStatus.DELETED

    @Transient
    private var domainEvents: MutableList<Any> = mutableListOf()

    /**
     * 도메인 이벤트 추가
     */
    private fun addDomainEvent(event: Any) {
        domainEvents.add(event)
    }

    /**
     * 도메인 이벤트를 조회 및 초기화하고 ID를 설정합니다.
     */
    override fun drainDomainEvents(): List<Any> {
        val events = domainEvents.toList()
        domainEvents.clear()

        // 이벤트의 commentId를 실제 ID로 설정
        val actualId = this.requireId()
        return events.map { event ->
            when (event) {
                is CommentCreatedEvent -> event.copy(commentId = actualId)
                is CommentUpdatedEvent -> event.copy(commentId = actualId)
                is CommentDeletedEvent -> event.copy(commentId = actualId)
                else -> event
            }
        }
    }
}
