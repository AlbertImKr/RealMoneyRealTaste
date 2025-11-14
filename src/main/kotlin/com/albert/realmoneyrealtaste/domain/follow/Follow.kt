package com.albert.realmoneyrealtaste.domain.follow

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand
import com.albert.realmoneyrealtaste.domain.follow.value.FollowRelationship
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 * 팔로우 관계 애그리거트 루트
 *
 * 비즈니스 규칙:
 * - 팔로우는 일방향 관계 (A가 B를 팔로우 != B가 A를 팔로우)
 * - 즉시 적용 (승인 과정 없음)
 * - 자기 자신은 팔로우 불가
 * - 중복 팔로우 방지
 * - 비활성화된 회원은 팔로우 불가
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["follower_id", "following_id"])
    ],
    indexes = [
        Index(name = "idx_follow_follower_id", columnList = "follower_id"),
        Index(name = "idx_follow_following_id", columnList = "following_id"),
        Index(name = "idx_follow_status", columnList = "status"),
        Index(name = "idx_follow_created_at", columnList = "created_at")
    ]
)
class Follow protected constructor(
    @Embedded
    val relationship: FollowRelationship,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: FollowStatus,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    var updatedAt: LocalDateTime,
) : BaseEntity() {

    companion object {
        const val ERROR_CANNOT_UNFOLLOW_INACTIVE = "활성 상태인 팔로우만 해제할 수 있습니다"
        const val ERROR_CANNOT_REACTIVATE_ACTIVE = "이미 활성 상태인 팔로우입니다"

        /**
         * 새로운 팔로우 관계 생성
         */
        fun create(command: FollowCreateCommand): Follow {
            val now = LocalDateTime.now()
            return Follow(
                relationship = FollowRelationship(
                    followerId = command.followerId,
                    followingId = command.followingId,
                ),
                status = FollowStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    /**
     * 팔로우 해제 (언팔로우)
     */
    fun unfollow() {
        require(status == FollowStatus.ACTIVE) { ERROR_CANNOT_UNFOLLOW_INACTIVE }

        status = FollowStatus.UNFOLLOWED
        updatedAt = LocalDateTime.now()
    }

    /**
     * 팔로우 재활성화 (차단 해제 후 등)
     */
    fun reactivate() {
        require(status != FollowStatus.ACTIVE) { ERROR_CANNOT_REACTIVATE_ACTIVE }

        status = FollowStatus.ACTIVE
        updatedAt = LocalDateTime.now()
    }

    /**
     * 팔로우 차단 (스팸 등의 이유로)
     */
    fun block() {
        status = FollowStatus.BLOCKED
        updatedAt = LocalDateTime.now()
    }

    /**
     * 특정 회원이 팔로워인지 확인
     */
    fun isFollowedBy(memberId: Long): Boolean {
        return relationship.isFollower(memberId) && status == FollowStatus.ACTIVE
    }

    /**
     * 특정 회원을 팔로잉하는지 확인
     */
    fun isFollowing(memberId: Long): Boolean {
        return relationship.isFollowing(memberId) && status == FollowStatus.ACTIVE
    }

    /**
     * 특정 회원과 관련된 팔로우 관계인지 확인
     */
    fun isRelatedTo(memberId: Long): Boolean {
        return relationship.isFollower(memberId) || relationship.isFollowing(memberId)
    }
}
