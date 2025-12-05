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
        JOIN Member m ON f.relationShip.memberId = m.id
        WHERE f.relationShip.friendMemberId = :friendMemberId AND f.status = :status
        AND m.status = 'ACTIVE'
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
        JOIN Member m ON f.relationShip.friendMemberId = m.id
        WHERE f.relationShip.memberId = :memberId 
        AND f.status = :status
        AND m.status = 'ACTIVE'
        ORDER BY f.createdAt DESC
        """
    )
    fun findSentFriendships(
        memberId: Long,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    @Query(
        """
        SELECT COUNT(f) > 0 
        FROM Friendship f 
        WHERE f.relationShip.memberId = :memberId 
        AND f.relationShip.friendMemberId = :friendId
        AND f.status = :status
        AND EXISTS (
            SELECT 1 FROM Friendship f2 
            WHERE f2.relationShip.friendMemberId = :memberId 
            AND f2.relationShip.memberId = :friendId
            AND f2.status = :status
        )
    """
    )
    fun isFriend(memberId: Long, friendId: Long, status: FriendshipStatus): Boolean

    fun findByRelationShipMemberIdAndRelationShipFriendMemberId(memberId: Long, friendMemberId: Long): Friendship?

    /**
     * 특정 회원의 양방향 친구 관계가 활성화된 친구 수를 계산합니다.
     */
    @Query(
        """
        SELECT COUNT(DISTINCT f1.relationShip.friendMemberId) FROM Friendship f1
        WHERE f1.relationShip.memberId = :memberId 
        AND f1.status = :status
        AND EXISTS (
            SELECT 1 FROM Friendship f2 
            WHERE f2.relationShip.friendMemberId = :memberId 
            AND f2.relationShip.memberId = f1.relationShip.friendMemberId 
            AND f2.status = :status
        )
        """
    )
    fun countFriends(memberId: Long, status: FriendshipStatus): Long

    fun findByRelationShipMemberIdAndRelationShipFriendMemberIdAndStatus(
        memberId: Long,
        friendMemberId: Long,
        status: FriendshipStatus,
    ): Friendship?

    @Query(
        """
        SELECT f1 FROM Friendship f1
        JOIN Member m ON f1.relationShip.friendMemberId = m.id
        WHERE f1.relationShip.memberId = :memberId 
        AND f1.status = :status
        AND m.status = 'ACTIVE'
        AND EXISTS (
            SELECT 1 FROM Friendship f2 
            WHERE f2.relationShip.friendMemberId = :memberId 
            AND f2.relationShip.memberId = f1.relationShip.friendMemberId 
            AND f2.status = :status
        )
    """
    )
    fun findMutualFriendships(
        memberId: Long,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    fun countFriendshipsByRelationShipFriendMemberIdAndStatus(friendMemberId: Long, status: FriendshipStatus): Long

    /**
     * 특정 회원의 친구를 키워드로 검색합니다.
     */
    @Query(
        """
        SELECT f1 FROM Friendship f1
        JOIN Member m ON f1.relationShip.friendMemberId = m.id
        WHERE f1.relationShip.memberId = :memberId 
        AND f1.status = :status
        AND m.status = 'ACTIVE'
        AND EXISTS (
            SELECT 1 FROM Friendship f2 
            WHERE f2.relationShip.friendMemberId = :memberId 
            AND f2.relationShip.memberId = f1.relationShip.friendMemberId 
            AND f2.status = :status
        )
        AND f1.relationShip.friendNickname LIKE %:keyword%
        ORDER BY f1.createdAt DESC
        """
    )
    fun searchFriendsByKeyword(
        memberId: Long,
        keyword: String,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    /**
     * 특정 회원의 최근 친구 목록을 조회합니다.
     */
    @Query(
        """
        SELECT f1 FROM Friendship f1
        JOIN Member m ON f1.relationShip.friendMemberId = m.id
        WHERE f1.relationShip.memberId = :memberId 
        AND f1.status = :status
        AND m.status = 'ACTIVE'
        AND EXISTS (
            SELECT 1 FROM Friendship f2 
            WHERE f2.relationShip.friendMemberId = :memberId 
            AND f2.relationShip.memberId = f1.relationShip.friendMemberId 
            AND f2.status = :status
        )
        ORDER BY f1.createdAt DESC
        """
    )
    fun findRecentFriends(
        memberId: Long,
        status: FriendshipStatus,
        pageable: Pageable,
    ): Page<Friendship>

    fun existsByRelationShipMemberIdAndRelationShipFriendMemberIdAndStatusIsIn(
        memberId: Long,
        friendMemberId: Long,
        statusList: List<FriendshipStatus>,
    ): Boolean

    /**
     * 특정 회원과 관련된 모든 활성 친구 관계를 조회합니다.
     * (회원이 요청자이거나 수신자인 모든 관계)
     */
    @Query(
        """
        SELECT f FROM Friendship f
        WHERE (f.relationShip.memberId = :memberId OR f.relationShip.friendMemberId = :memberId)
        AND f.status = 'ACCEPTED'
        """
    )
    fun findAllActiveByMemberId(memberId: Long): List<Friendship>
}
