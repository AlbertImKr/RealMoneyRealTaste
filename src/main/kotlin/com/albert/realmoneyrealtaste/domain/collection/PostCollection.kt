package com.albert.realmoneyrealtaste.domain.collection

import com.albert.realmoneyrealtaste.domain.collection.command.CollectionCreateCommand
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionInfo
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionOwner
import com.albert.realmoneyrealtaste.domain.collection.value.CollectionPosts
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
    name = "post_collections",
    indexes = [
        Index(name = "idx_collection_owner_member_id", columnList = "owner_member_id"),
        Index(name = "idx_collection_privacy_status", columnList = "privacy, status"),
        Index(name = "idx_collection_created_at", columnList = "created_at"),
        Index(name = "idx_collection_status", columnList = "status")
    ]
)
class PostCollection protected constructor(
    owner: CollectionOwner,
    info: CollectionInfo,
    posts: CollectionPosts,
    privacy: CollectionPrivacy,
    status: CollectionStatus,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) : BaseEntity() {

    @Embedded
    var owner: CollectionOwner = owner
        protected set

    @Embedded
    var info: CollectionInfo = info
        protected set

    @Embedded
    var posts: CollectionPosts = posts
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    var privacy: CollectionPrivacy = privacy
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CollectionStatus = status
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    /**
     * 컬렉션 정보를 수정합니다.
     *
     * @param memberId 수정하려는 회원 ID
     * @param newInfo 새로운 컬렉션 정보
     * @throws IllegalArgumentException 수정 권한이 없거나 컬렉션이 비활성 상태인 경우 발생
     */
    fun updateInfo(memberId: Long, newInfo: CollectionInfo) {
        ensureCanEditBy(memberId)
        ensureActive()
        this.info = newInfo
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 컬렉션 공개 설정을 변경합니다.
     *
     * @param memberId 변경하려는 회원 ID
     * @param newPrivacy 새로운 공개 설정
     * @throws IllegalArgumentException 변경 권한이 없거나 컬렉션이 비활성 상태인 경우 발생
     */
    fun updatePrivacy(memberId: Long, newPrivacy: CollectionPrivacy) {
        ensureCanEditBy(memberId)
        ensureActive()
        this.privacy = newPrivacy
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 컬렉션에 게시글을 추가합니다.
     *
     * @param memberId 추가하려는 회원 ID
     * @param postId 추가할 게시글 ID
     * @throws IllegalArgumentException 추가 권한이 없거나 컬렉션이 비활성 상태인 경우 발생
     */
    fun addPost(memberId: Long, postId: Long) {
        ensureCanEditBy(memberId)
        ensureActive()

        require(!posts.contains(postId)) { "이미 컬렉션에 포함된 게시글입니다: $postId" }

        this.posts = posts.add(postId)
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 컬렉션에서 게시글을 제거합니다.
     *
     * @param memberId 제거하려는 회원 ID
     * @param postId 제거할 게시글 ID
     * @throws IllegalArgumentException 제거 권한이 없거나 컬렉션이 비활성 상태인 경우 발생
     */
    fun removePost(memberId: Long, postId: Long) {
        ensureCanEditBy(memberId)
        ensureActive()

        require(posts.contains(postId)) { "컬렉션에 존재하지 않는 게시글입니다: $postId" }

        this.posts = posts.remove(postId)
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 컬렉션을 삭제합니다 (Soft Delete).
     *
     * @param memberId 삭제하려는 회원 ID
     * @throws IllegalArgumentException 삭제 권한이 없거나 컬렉션이 비활성 상태인 경우 발생
     */
    fun delete(memberId: Long) {
        ensureCanEditBy(memberId)
        ensureActive()
        this.status = CollectionStatus.DELETED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 회원이 이 컬렉션을 수정할 권한이 있는지 확인합니다.
     *
     * @param memberId 회원 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    fun canEditBy(memberId: Long): Boolean {
        return owner.memberId == memberId
    }

    /**
     * 회원이 이 컬렉션을 조회할 권한이 있는지 확인합니다.
     * 공개 컬렉션이거나 소유자인 경우 조회 가능합니다.
     *
     * @param memberId 회원 ID (null인 경우 비로그인 사용자)
     * @return 권한이 있으면 true, 없으면 false
     */
    fun canViewBy(memberId: Long?): Boolean {
        if (isDeleted()) return false
        if (privacy == CollectionPrivacy.PUBLIC) return true
        return memberId != null && owner.memberId == memberId
    }

    /**
     * 회원이 이 컬렉션을 수정할 수 없다면 예외를 발생시킵니다.
     *
     * @param memberId 회원 ID
     * @throws IllegalArgumentException 수정 권한이 없는 경우 발생
     */
    fun ensureCanEditBy(memberId: Long) {
        require(canEditBy(memberId)) { "컬렉션을 수정할 권한이 없습니다." }
    }

    /**
     * 컬렉션이 활성 상태인지 확인합니다.
     *
     * @throws IllegalArgumentException 활성 상태가 아닌 경우 발생
     */
    fun ensureActive() {
        require(status == CollectionStatus.ACTIVE) { "컬렉션이 활성 상태가 아닙니다: $status" }
    }

    /**
     * 컬렉션이 공개되어 있는지 확인합니다.
     *
     * @return 공개 상태이면 true, 비공개이면 false
     */
    fun isPublic(): Boolean = privacy == CollectionPrivacy.PUBLIC

    /**
     * 컬렉션이 비공개인지 확인합니다.
     *
     * @return 비공개 상태이면 true, 공개이면 false
     */
    fun isPrivate(): Boolean = privacy == CollectionPrivacy.PRIVATE

    /**
     * 컬렉션이 삭제되었는지 확인합니다.
     *
     * @return 삭제 상태이면 true, 활성 상태이면 false
     */
    fun isDeleted(): Boolean = status == CollectionStatus.DELETED

    /**
     * 컬렉션에 포함된 게시글 수를 반환합니다.
     *
     * @return 게시글 수
     */
    fun getPostCount(): Int = posts.size()

    /**
     * 컬렉션이 비어있는지 확인합니다.
     *
     * @return 비어있으면 true, 게시글이 있으면 false
     */
    fun isEmpty(): Boolean = posts.isEmpty()

    fun postCounts(): Int = posts.size()

    companion object {
        /**
         * 새로운 컬렉션을 생성합니다.
         *
         * @param createCommand 생성 명령 객체
         * @return 생성된 컬렉션
         */
        fun create(createCommand: CollectionCreateCommand): PostCollection {
            val now = LocalDateTime.now()
            return PostCollection(
                owner = CollectionOwner(createCommand.ownerMemberId),
                info = CollectionInfo(
                    name = createCommand.name,
                    description = createCommand.description,
                    coverImageUrl = createCommand.coverImageUrl
                ),
                posts = CollectionPosts.empty(),
                privacy = createCommand.privacy,
                status = CollectionStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
