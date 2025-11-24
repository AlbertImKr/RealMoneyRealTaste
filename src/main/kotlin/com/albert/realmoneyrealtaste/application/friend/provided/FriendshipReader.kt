package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.application.friend.dto.FriendshipResponse
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 친구 관계 조회 포트
 */
interface FriendshipReader {

    /**
     * 활성화된 친구 관계를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param friendMemberId 친구 회원 ID
     * @return 활성화된 친구 관계 또는 null
     */
    fun findActiveFriendship(memberId: Long, friendMemberId: Long): Friendship?

    /**
     * 두 회원 간의 친구 관계를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param friendMemberId 친구 회원 ID
     * @return 친구 관계 또는 null
     */
    fun sentedFriendRequest(memberId: Long, friendMemberId: Long): Friendship?

    /**
     * 수신된 대기 중인 친구 요청을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param fromMemberId 요청 보낸 회원 ID
     * @return 대기 중인 친구 요청 또는 null
     */
    fun findPendingFriendshipReceived(memberId: Long, fromMemberId: Long): Friendship?

    /**
     * 친구 관계를 ID로 조회합니다.
     *
     * @param friendshipId 친구 관계 ID
     * @return 조회된 친구 관계
     */
    fun findFriendshipById(friendshipId: Long): Friendship

    /**
     * 특정 회원의 친구 목록을 페이징 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 친구 목록 페이지
     */
    fun findFriendsByMemberId(memberId: Long, pageable: Pageable): Page<FriendshipResponse>

    /**
     * 특정 회원이 받은 대기 중인 친구 요청 목록을 페이징 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 받은 대기 중인 친구 요청 목록 페이지
     */
    fun findPendingRequestsReceived(memberId: Long, pageable: Pageable): Page<FriendshipResponse>

    /**
     * 특정 회원이 보낸 대기 중인 친구 요청 목록을 페이징 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 보낸 대기 중인 친구 요청 목록 페이지
     */
    fun findPendingRequestsSent(memberId: Long, pageable: Pageable): Page<FriendshipResponse>

    /**
     * 두 회원 간의 친구 관계 존재 여부를 확인합니다.
     *
     * @param memberId 회원 ID
     * @param friendMemberId 친구 회원 ID
     * @return 친구 관계가 존재하면 true, 그렇지 않으면 false
     */
    fun isFriend(memberId: Long, friendMemberId: Long): Boolean

    /**
     * 특정 회원의 친구 수를 계산합니다.
     *
     * @param memberId 회원 ID
     * @return 친구 수
     */
    fun countFriendsByMemberId(memberId: Long): Long

    /**
     * 특정 회원의 친구를 키워드로 검색합니다.
     *
     * @param memberId 회원 ID
     * @param keyword 검색 키워드 (닉네임, 이메일 등)
     * @param pageable 페이징 정보
     * @return 검색된 친구 목록 페이지
     */
    fun searchFriends(memberId: Long, keyword: String, pageable: Pageable): Page<FriendshipResponse>

    /**
     * 특정 회원의 최근 친구 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 최근 친구 목록
     */
    fun findRecentFriends(memberId: Long, pageable: Pageable): Page<FriendshipResponse>

    /**
     * 대기 중인 친구 요청 수를 계산합니다.
     *
     * @param memberId 회원 ID
     * @return 대기 중인 친구 요청 수
     */
    fun countPendingRequests(memberId: Long): Long

    fun findPendingRequests(memberId: Long, pageable: Pageable): Page<Friendship>

    fun findByMembersId(memberId: Long, friendMemberId: Long): Friendship?
}
