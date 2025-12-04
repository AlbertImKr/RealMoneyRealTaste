package com.albert.realmoneyrealtaste.application.follow.required

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

/**
 * 팔로우 저장소 포트
 */
interface FollowRepository : Repository<Follow, Long> {

    fun save(follow: Follow): Follow

    fun findById(id: Long): Follow?

    /**
     * 팔로워 ID와 팔로잉 ID로 팔로우 관계 조회
     */
    @Query(
        """
        SELECT f FROM Follow f
        WHERE f.relationship.followerId = :followerId
          AND f.relationship.followingId = :followingId
          AND f.status = :status
    """
    )
    fun findFollowerByRelationshipAndStatus(
        followerId: Long,
        followingId: Long,
        status: FollowStatus,
    ): Follow?

    @Query(
        """
        SELECT f FROM Follow f
        JOIN Member m ON f.relationship.followerId = m.id
        WHERE f.relationship.followingId = :followingId 
        AND f.status = :status
        AND m.status = 'ACTIVE'
    """
    )
    fun findFollowersByFollowingIdAndStatus(followingId: Long, status: FollowStatus, pageable: Pageable): Page<Follow>

    @Query(
        """
        SELECT f FROM Follow f
        JOIN Member m ON f.relationship.followingId = m.id
        WHERE f.relationship.followerId = :followerId AND f.status = :status
        AND m.status = 'ACTIVE'
    """
    )
    fun findFollowingsByFollowerIdAndStatus(followerId: Long, status: FollowStatus, pageable: Pageable): Page<Follow>

    @Query(
        """
        SELECT COUNT(f) FROM Follow f
        WHERE f.relationship.followingId = :followingId
          AND f.status = :status
    """
    )
    fun countByFollowingIdAndStatus(followingId: Long, status: FollowStatus): Long

    @Query(
        """
        SELECT COUNT(f) FROM Follow f
        WHERE f.relationship.followerId = :followerId
          AND f.status = :status
    """
    )
    fun countByFollowerIdAndStatus(followerId: Long, status: FollowStatus): Long

    /**
     * 팔로워 ID와 팔로잉 ID로 팔로우 관계 존재 여부 확인
     */
    @Query(
        """
        SELECT COUNT(f) > 0 FROM Follow f
        WHERE f.relationship.followerId = :followerId
          AND f.relationship.followingId = :followingId
          AND f.status = :status
    """
    )
    fun existsByRelationshipAndStatus(followerId: Long, followingId: Long, status: FollowStatus): Boolean

    @Query(
        """
            SELECT f.relationship.followingId
            FROM Follow f
            WHERE f.relationship.followerId = :followerId
              AND f.relationship.followingId IN :followingIds
              AND f.status = :status
        """
    )
    fun findFollowingByIds(followerId: Long, followingIds: List<Long>, status: FollowStatus): List<Long>

    @Query(
        """
            SELECT f.relationship.followerId
            FROM Follow f
            WHERE f.relationship.followingId = :followingId
              AND f.relationship.followerId IN :followerIds
              AND f.status = :status
        """
    )
    fun findFollowerByIds(followingId: Long, followerIds: List<Long>, status: FollowStatus): List<Long>

    /**
     * 팔로워 ID와 팔로잉 ID로 팔로우 관계 조회 (상태 무관)
     */
    @Query(
        """
        SELECT f FROM Follow f
        WHERE f.relationship.followerId = :followerId
          AND f.relationship.followingId = :followingId
    """
    )
    fun findByRelationship(followerId: Long, followingId: Long): Follow?

    /**
     * 팔로워 검색 (팔로워의 닉네임으로 검색)
     * 내가 팔로우 받는 사람들 중에서 검색
     */
    @Query(
        """
        SELECT f FROM Follow f
        JOIN Member m ON f.relationship.followerId = m.id
        WHERE f.relationship.followingId = :memberId
          AND f.status = :status
          AND f.relationship.followerNickname LIKE %:keyword%
          AND m.status = 'ACTIVE'
    """
    )
    fun searchFollowersByFollowingIdAndStatus(
        memberId: Long,
        keyword: String,
        status: FollowStatus,
        pageable: Pageable,
    ): Page<Follow>

    /**
     * 팔로잉 검색 (팔로잉 대상의 닉네임으로 검색)
     * 내가 팔로우하는 사람들 중에서 검색
     */
    @Query(
        """
        SELECT f FROM Follow f
        JOIN Member m ON f.relationship.followingId = m.id
        WHERE f.relationship.followerId = :memberId
          AND f.status = :status
          AND f.relationship.followingNickname LIKE %:keyword%
          AND m.status = 'ACTIVE'
    """
    )
    fun searchFollowingsByFollowerIdAndStatus(
        memberId: Long,
        keyword: String,
        status: FollowStatus,
        pageable: Pageable,
    ): Page<Follow>
}
