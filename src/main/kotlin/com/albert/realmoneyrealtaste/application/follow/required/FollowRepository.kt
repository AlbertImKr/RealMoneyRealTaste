package com.albert.realmoneyrealtaste.application.follow.required

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.domain.follow.value.FollowRelationship
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

    fun findFollowerByRelationshipAndStatus(relationship: FollowRelationship, status: FollowStatus): Follow?

    @Query(
        """
        SELECT f FROM Follow f
        WHERE f.relationship.followingId = :followingId AND f.status = :status
    """
    )
    fun findFollowersByFollowingIdAndStatus(followingId: Long, status: FollowStatus, pageable: Pageable): Page<Follow>

    @Query(
        """
        SELECT f FROM Follow f
        WHERE f.relationship.followerId = :followerId AND f.status = :status
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

    fun existsByRelationshipAndStatus(relationship: FollowRelationship, status: FollowStatus): Boolean

    fun findByRelationship(relationship: FollowRelationship): Follow?

    @Query(
        """
        SELECT f FROM Follow f
        WHERE f.relationship.followerId = :memberId
          AND f.status = :status
        ORDER BY FUNCTION('RAND')
    """
    )
    fun findSuggestedUsersByMemberIdAndStatus(
        memberId: Long,
        status: FollowStatus,
        pageable: Pageable,
    ): Page<Follow>
}
