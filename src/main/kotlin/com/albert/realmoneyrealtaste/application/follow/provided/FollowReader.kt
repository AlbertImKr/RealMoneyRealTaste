package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.application.follow.dto.FollowResponse
import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.domain.follow.Follow
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 팔로우 관계 조회 포트
 */
interface FollowReader {

    fun findActiveFollow(followerId: Long, followingId: Long): Follow

    fun findFollowByRelationship(followerId: Long, followingId: Long): Follow?

    fun findFollowById(followId: Long): Follow

    fun findFollowersByMemberId(memberId: Long, pageable: Pageable): Page<FollowResponse>

    fun findFollowingsByMemberId(memberId: Long, pageable: Pageable): Page<FollowResponse>

    /**
     * 팔로워 검색
     */
    fun searchFollowers(memberId: Long, keyword: String, pageable: Pageable): Page<FollowResponse>

    /**
     * 팔로잉 검색
     */
    fun searchFollowings(memberId: Long, keyword: String, pageable: Pageable): Page<FollowResponse>

    fun checkIsFollowing(followerId: Long, followingId: Long): Boolean

    fun checkIsMutualFollow(member1Id: Long, member2Id: Long): Boolean

    fun getFollowStats(memberId: Long): FollowStatsResponse

    fun existsActiveFollow(followerId: Long, followingId: Long): Boolean

    fun findSuggestedUsers(memberId: Long, limit: Int): List<FollowResponse>
}
