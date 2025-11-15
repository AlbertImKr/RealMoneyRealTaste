package com.albert.realmoneyrealtaste.domain.post

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.post.value.Author
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
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
    name = "posts",
    indexes = [
        Index(name = "idx_post_author_id", columnList = "author_member_id"),
        Index(name = "idx_post_status", columnList = "status"),
        Index(name = "idx_post_created_at", columnList = "created_at"),
        Index(name = "idx_post_restaurant_name", columnList = "restaurant_name")
    ]
)
class Post protected constructor(
    author: Author,

    restaurant: Restaurant,

    content: PostContent,

    images: PostImages,

    status: PostStatus,

    heartCount: Int,

    viewCount: Int,

    createdAt: LocalDateTime,

    updatedAt: LocalDateTime,
) : BaseEntity() {

    companion object {
        const val ERROR_NO_EDIT_PERMISSION = "게시글 수정 권한이 없습니다."
        const val ERROR_NOT_PUBLISHED = "게시글이 공개 상태가 아닙니다."

        fun create(
            authorMemberId: Long,
            authorNickname: String,
            restaurant: Restaurant,
            content: PostContent,
            images: PostImages,
            authorIntroduction: String = "",
        ): Post {
            return Post(
                author = Author(authorMemberId, authorNickname, authorIntroduction),
                restaurant = restaurant,
                content = content,
                images = images,
                status = PostStatus.PUBLISHED,
                heartCount = 0,
                viewCount = 0,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }

    @Embedded
    var author: Author = author
        protected set

    @Embedded
    var restaurant: Restaurant = restaurant
        protected set

    @Embedded
    var content: PostContent = content
        protected set

    @Embedded
    var images: PostImages = images
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PostStatus = status
        protected set

    @Column(name = "heart_count", nullable = false)
    var heartCount: Int = heartCount
        protected set

    @Column(name = "view_count", nullable = false)
    var viewCount: Int = viewCount
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    /**
     * 게시글 내용을 수정합니다.
     *
     * @throws IllegalArgumentException 수정 권한이 없는 경우 혹은 게시글이 공개 상태가 아닌 경우
     */
    fun update(memberId: Long, content: PostContent, images: PostImages, restaurant: Restaurant) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.content = content
        this.images = images
        this.restaurant = restaurant
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 게시글을 삭제합니다 (Soft Delete).
     *
     * @throws IllegalArgumentException 삭제 권한이 없는 경우 혹은 게시글이 공개 상태가 아닌 경우
     */
    fun delete(memberId: Long) {
        ensureCanEditBy(memberId)
        ensurePublished()
        this.status = PostStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 회원이 이 게시글을 수정할 권한이 있는지 확인합니다.
     *
     * @param memberId 회원 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    fun canEditBy(memberId: Long): Boolean {
        return author.memberId == memberId
    }

    /**
     * 회원이 이 게시글을 수정할 수 없다면 예외를 발생시킵니다.
     *
     * @param memberId 회원 ID
     * @throws IllegalArgumentException 수정 권한이 없는 경우
     */
    fun ensureCanEditBy(memberId: Long) {
        require(canEditBy(memberId)) { ERROR_NO_EDIT_PERMISSION }
    }

    /**
     * 게시글이 공개 상태인지 확인합니다.
     *
     * @throws IllegalArgumentException 게시글이 공개 상태가 아닌 경우
     */
    fun ensurePublished() {
        require(status == PostStatus.PUBLISHED) { ERROR_NOT_PUBLISHED }
    }

    fun isAuthor(memberId: Long): Boolean = author.memberId == memberId

    fun isDeleted(): Boolean = status == PostStatus.DELETED
}
