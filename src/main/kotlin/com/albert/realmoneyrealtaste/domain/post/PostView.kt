package com.albert.realmoneyrealtaste.domain.post

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "post_views",
    indexes = [
        Index(name = "idx_post_view_post_id", columnList = "post_id"),
        Index(name = "idx_post_view_member_id", columnList = "member_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_view_post_member",
            columnNames = ["post_id", "member_id"]
        )
    ]
)
class PostView protected constructor(
    postId: Long,

    memberId: Long,

    viewAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "post_id", nullable = false)
    var postId: Long = postId
        protected set

    @Column(name = "member_id", nullable = false)
    var memberId: Long = memberId
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    var viewAt: LocalDateTime = viewAt
        protected set

    companion object {
        /**
         * PostView를 생성합니다.
         *
         * @param postId 게시글 ID
         * @param memberId 회원 ID
         * @return 생성된 PostView
         */
        fun create(postId: Long, memberId: Long): PostView {
            return PostView(
                postId = postId,
                memberId = memberId,
                viewAt = LocalDateTime.now()
            )
        }
    }
}
