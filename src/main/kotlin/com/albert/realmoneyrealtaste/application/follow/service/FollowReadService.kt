package com.albert.realmoneyrealtaste.application.follow.service

import com.albert.realmoneyrealtaste.application.follow.dto.FollowResponse
import com.albert.realmoneyrealtaste.application.follow.dto.FollowStatsResponse
import com.albert.realmoneyrealtaste.application.follow.exception.FollowNotFoundException
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.domain.follow.value.FollowRelationship
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FollowReadService(
    private val followRepository: FollowRepository,
    private val memberReader: MemberReader,
) : FollowReader {

    companion object {
        const val ERROR_FOLLOW_NOT_FOUND = "팔로우 관계를 찾을 수 없습니다."

        const val UNKNOWN_NICKNAME = "Unknown"
    }

    override fun findActiveFollow(followerId: Long, followingId: Long): Follow {
        return followRepository.findFollowerByRelationshipAndStatus(
            FollowRelationship(followerId, followingId),
            FollowStatus.ACTIVE
        ) ?: throw FollowNotFoundException(ERROR_FOLLOW_NOT_FOUND)
    }

    override fun findFollowByRelationship(followerId: Long, followingId: Long): Follow? {
        return followRepository.findByRelationship(FollowRelationship(followerId, followingId))
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

    override fun checkIsFollowing(followerId: Long, followingId: Long): Boolean {
        return followRepository.existsByRelationshipAndStatus(
            FollowRelationship(followerId, followingId),
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
            FollowRelationship(followerId, followingId),
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

    override fun findSuggestedUsers(
        memberId: Long,
        limit: Int,
    ): List<FollowResponse> {
        val follows = followRepository.findSuggestedUsersByMemberIdAndStatus(
            memberId,
            FollowStatus.ACTIVE,
            Pageable.ofSize(limit)
        )
        return mapToFollowResponses(follows).content
    }

    private fun mapToFollowResponses(follows: Page<Follow>): Page<FollowResponse> {
        return follows.map { follow ->
            val memberIds = listOf(
                follow.relationship.followerId,
                follow.relationship.followingId
            )
            val members = memberReader.readAllActiveMembersByIds(memberIds)
            val memberMap = members.associate { it.id to it.nickname.value }

            FollowResponse.from(
                follow,
                memberMap[follow.relationship.followerId] ?: UNKNOWN_NICKNAME,
                memberMap[follow.relationship.followingId] ?: UNKNOWN_NICKNAME,
            )
        }
    }
}
