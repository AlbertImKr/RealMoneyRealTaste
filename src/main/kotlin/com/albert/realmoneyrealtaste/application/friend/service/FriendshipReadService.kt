package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.friend.dto.FriendshipResponse
import com.albert.realmoneyrealtaste.application.friend.exception.FriendshipNotFoundException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.value.FriendRelationship
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FriendshipReadService(
    private val friendshipRepository: FriendshipRepository,
    private val memberReader: MemberReader,
) : FriendshipReader {

    companion object {
        const val ERROR_FRIENDSHIP_NOT_FOUND = "친구 관계를 찾을 수 없습니다."

        const val UNKNOWN_NICKNAME = "Unknown"
    }

    override fun findActiveFriendship(memberId: Long, friendMemberId: Long): Friendship? {
        return friendshipRepository.findByRelationShipAndStatus(
            FriendRelationship(memberId, friendMemberId),
            FriendshipStatus.ACCEPTED
        )
    }

    override fun findFriendshipBetweenMembers(
        memberId: Long,
        friendMemberId: Long,
    ): Friendship? {
        return friendshipRepository.findByRelationShip(FriendRelationship(memberId, friendMemberId))
    }

    override fun findPendingFriendshipReceived(memberId: Long, fromMemberId: Long): Friendship? {
        return friendshipRepository.findByRelationShipAndStatus(
            FriendRelationship(fromMemberId, memberId),
            FriendshipStatus.PENDING
        )
    }

    override fun findFriendshipById(friendshipId: Long): Friendship {
        return friendshipRepository.findById(friendshipId)
            ?: throw FriendshipNotFoundException(ERROR_FRIENDSHIP_NOT_FOUND)
    }

    override fun findFriendsByMemberId(memberId: Long, pageable: Pageable): Page<FriendshipResponse> {
        val friendships = friendshipRepository.findMutualFriendships(
            memberId, FriendshipStatus.ACCEPTED, pageable
        )

        return mapToFriendshipResponses(friendships)
    }

    override fun findPendingRequestsReceived(memberId: Long, pageable: Pageable): Page<FriendshipResponse> {
        val requests = friendshipRepository.findReceivedFriendships(
            memberId, FriendshipStatus.PENDING, pageable
        )
        return mapToFriendshipResponses(requests)
    }

    override fun findPendingRequestsSent(memberId: Long, pageable: Pageable): Page<FriendshipResponse> {
        val requests = friendshipRepository.findSentFriendships(
            memberId, FriendshipStatus.PENDING, pageable
        )
        return mapToFriendshipResponses(requests)
    }

    private fun mapToFriendshipResponses(friendships: Page<Friendship>): Page<FriendshipResponse> {
        return friendships
            .map { friendship ->
                FriendshipResponse.from(
                    friendship,
                    friendship.relationShip.friendNickname ?: UNKNOWN_NICKNAME,
                    UNKNOWN_NICKNAME, // memberNickname은 필요시 별도 조회
                )
            }
    }

    override fun existsByMemberIds(memberId: Long, friendMemberId: Long): Boolean {
        return friendshipRepository.existsByRelationShip(FriendRelationship(memberId, friendMemberId))
    }

    override fun countFriendsByMemberId(memberId: Long): Long {
        return friendshipRepository.countFriendshipsByRelationShipMemberIdAndStatus(memberId, FriendshipStatus.ACCEPTED)
    }

    override fun searchFriends(memberId: Long, keyword: String, pageable: Pageable): Page<FriendshipResponse> {
        val friendships = friendshipRepository.searchFriendsByKeyword(
            memberId, keyword, FriendshipStatus.ACCEPTED, pageable
        )

        return mapToFriendshipResponses(friendships)
    }

    override fun findRecentFriends(memberId: Long, limit: Int): List<FriendshipResponse> {
        val friendships = friendshipRepository.findRecentFriends(
            memberId, FriendshipStatus.ACCEPTED, limit
        )

        return friendships.map { friendship ->
            FriendshipResponse.from(
                friendship,
                friendship.relationShip.friendNickname ?: UNKNOWN_NICKNAME,
                UNKNOWN_NICKNAME, // memberNickname은 필요시 별도 조회
            )
        }
    }
}
