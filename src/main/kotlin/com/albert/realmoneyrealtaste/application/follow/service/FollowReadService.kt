package com.albert.realmoneyrealtaste.application.follow.service

import com.albert.realmoneyrealtaste.application.follow.dto.FollowResponse
import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.application.follow.exception.FollowNotFoundException
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FollowReadService(
    private val followRepository: FollowRepository,
) : FollowReader {

    companion object {
        const val ERROR_FOLLOW_NOT_FOUND = "팔로우 관계를 찾을 수 없습니다."
    }

    override fun findActiveFollow(followerId: Long, followingId: Long): Follow {
        return followRepository.findFollowerByRelationshipAndStatus(
            followerId,
            followingId,
            FollowStatus.ACTIVE
        ) ?: throw FollowNotFoundException(ERROR_FOLLOW_NOT_FOUND)
    }

    override fun findFollowByRelationship(followerId: Long, followingId: Long): Follow? {
        return followRepository.findByRelationship(followerId, followingId)
    }

    override fun findFollowById(followId: Long): Follow {
        return followRepository.findById(followId)
            ?: throw FollowNotFoundException(ERROR_FOLLOW_NOT_FOUND)
    }

    override fun findFollowersByMemberId(memberId: Long, pageable: Pageable): Page<FollowResponse> {
        val follows = followRepository.findFollowersByFollowingIdAndStatus(
            memberId, FollowStatus.ACTIVE, pageable
        )
        return mapToFollowResponses(follows)
    }

    override fun findFollowingsByMemberId(memberId: Long, pageable: Pageable): Page<FollowResponse> {
        val follows = followRepository.findFollowingsByFollowerIdAndStatus(
            memberId, FollowStatus.ACTIVE, pageable
        )
        return mapToFollowResponses(follows)
    }

    override fun searchFollowers(memberId: Long, keyword: String, pageable: Pageable): Page<FollowResponse> {
        val follows = followRepository.searchFollowersByFollowingIdAndStatus(
            memberId, keyword, FollowStatus.ACTIVE, pageable
        )
        return mapToFollowResponses(follows)
    }

    override fun searchFollowings(memberId: Long, keyword: String, pageable: Pageable): Page<FollowResponse> {
        val follows = followRepository.searchFollowingsByFollowerIdAndStatus(
            memberId, keyword, FollowStatus.ACTIVE, pageable
        )
        return mapToFollowResponses(follows)
    }

    override fun checkIsFollowing(followerId: Long, followingId: Long): Boolean {
        return followRepository.existsByRelationshipAndStatus(
            followerId,
            followingId,
            FollowStatus.ACTIVE
        )
    }

    override fun findFollowings(
        followerId: Long,
        targetIds: List<Long>,
    ): List<Long> {
        return followRepository.findFollowingByIds(
            followerId,
            targetIds,
            FollowStatus.ACTIVE
        )
    }

    override fun findFollowers(
        followingId: Long,
        targetIds: List<Long>,
    ): List<Long> {
        return followRepository.findFollowerByIds(
            followingId,
            targetIds,
            FollowStatus.ACTIVE
        )
    }

    override fun checkIsMutualFollow(member1Id: Long, member2Id: Long): Boolean {
        val member1FollowsMember2 = checkIsFollowing(member1Id, member2Id)
        val member2FollowsMember1 = checkIsFollowing(member2Id, member1Id)
        return member1FollowsMember2 && member2FollowsMember1
    }

    override fun existsActiveFollow(followerId: Long, followingId: Long): Boolean {
        return followRepository.existsByRelationshipAndStatus(
            followerId,
            followingId,
            FollowStatus.ACTIVE
        )
    }

    override fun getFollowStats(memberId: Long): FollowStatsResponse {
        val followersCount = followRepository.countByFollowingIdAndStatus(
            memberId, FollowStatus.ACTIVE
        )
        val followingCount = followRepository.countByFollowerIdAndStatus(
            memberId, FollowStatus.ACTIVE
        )

        return FollowStatsResponse(
            memberId = memberId,
            followersCount = followersCount,
            followingCount = followingCount
        )
    }

    /**
     * Follow 엔티티를 FollowResponse로 변환
     * FollowRelationship에 닉네임이 포함되어 있으므로 직접 매핑
     */
    private fun mapToFollowResponses(follows: Page<Follow>): Page<FollowResponse> {
        return follows.map { follow -> FollowResponse.from(follow) }
    }
}
