package com.albert.realmoneyrealtaste.application.friend.required

import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.value.FriendRelationship
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

/**
 * 친구 관계 저장소 포트
 */
interface FriendshipRepository : Repository<Friendship, Long> {

    fun save(friendship: Friendship): Friendship

    fun findById(id: Long): Friendship?

    fun findByRelationShipAndStatus(relationShip: FriendRelationship, status: FriendshipStatus): Friendship?

    /**
     * 특정 회원의 친구 목록 또는 요청 목록을 상태별로 페이징 조회
     */
    @Query(
        """
        SELECT f FROM Friendship f
        WHERE f.relationShip.friendMemberId = :friendMemberId AND f.status = :status
        ORDER BY f.createdAt DESC
        """
    )
    fun findReceivedFriendships(
        friendMemberId: Long,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    /**
     * 특정 회원이 친구 요청을 보낸 대상 목록을 상태별로 페이징 조회
     */
    @Query(
        """
        SELECT f FROM Friendship f
        WHERE f.relationShip.memberId = :memberId AND f.status = :status
        ORDER BY f.createdAt DESC
        """
    )
    fun findSentFriendships(
        memberId: Long,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    fun existsByRelationShip(relationShip: FriendRelationship): Boolean

    fun findByRelationShip(relationShip: FriendRelationship): Friendship?
}
