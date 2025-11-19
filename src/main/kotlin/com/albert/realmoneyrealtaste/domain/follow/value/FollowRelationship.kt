package com.albert.realmoneyrealtaste.domain.follow.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 팔로우 관계 정보 (Value Object)
 * 팔로워와 팔로잉 대상의 ID와 닉네임을 포함합니다.
 */
@Embeddable
data class FollowRelationship(
    @Column(name = "follower_id", nullable = false)
    val followerId: Long,

    @Column(name = "follower_nickname", nullable = false, length = 50)
    val followerNickname: String,

    @Column(name = "following_id", nullable = false)
    val followingId: Long,

    @Column(name = "following_nickname", nullable = false, length = 50)
    val followingNickname: String,
) {
    init {
        require(followerId > 0) { "팔로워 ID는 양수여야 합니다" }
        require(followingId > 0) { "팔로잉 대상 ID는 양수여야 합니다" }
        require(followerId != followingId) { "자기 자신을 팔로우할 수 없습니다" }
        require(followerNickname.isNotBlank()) { "팔로워 닉네임은 비어있을 수 없습니다" }
        require(followingNickname.isNotBlank()) { "팔로잉 대상 닉네임은 비어있을 수 없습니다" }
    }

    /**
     * 특정 회원이 팔로워인지 확인
     */
    fun isFollower(memberId: Long): Boolean = followerId == memberId

    /**
     * 특정 회원이 팔로잉 대상인지 확인
     */
    fun isFollowing(memberId: Long): Boolean = followingId == memberId
}
