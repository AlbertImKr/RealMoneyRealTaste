package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.friend.dto.FriendshipResponse
import com.albert.realmoneyrealtaste.application.friend.exception.FriendshipNotFoundException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FriendshipReadService(
    private val friendshipRepository: FriendshipRepository,
) : FriendshipReader {

    companion object {
        const val ERROR_FRIENDSHIP_NOT_FOUND = "친구 관계를 찾을 수 없습니다."

        const val UNKNOWN_NICKNAME = "Unknown"
    }

    override fun findActiveFriendship(memberId: Long, friendMemberId: Long): Friendship? {
        return friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberIdAndStatus(
            memberId,
            friendMemberId,
            FriendshipStatus.ACCEPTED
        )
    }

    override fun sentedFriendRequest(
        memberId: Long,
        friendMemberId: Long,
    ): Friendship? {
        return friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberIdAndStatus(
            memberId,
            friendMemberId,
            FriendshipStatus.PENDING
        )
    }

    override fun findPendingFriendshipReceived(memberId: Long, fromMemberId: Long): Friendship? {
        return friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberIdAndStatus(
            fromMemberId, memberId,
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

    override fun existsByMemberIds(memberId: Long, friendMemberId: Long): Boolean {
        return friendshipRepository.existsBy(memberId, friendMemberId)
    }

    override fun findByMembersId(
        memberId: Long,
        friendMemberId: Long,
    ): Friendship? {
        return friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            memberId, friendMemberId
        )
    }

    override fun countFriendsByMemberId(memberId: Long): Long {
        return friendshipRepository.countFriends(memberId, FriendshipStatus.ACCEPTED)
    }

    override fun searchFriends(memberId: Long, keyword: String, pageable: Pageable): Page<FriendshipResponse> {
        val friendships = friendshipRepository.searchFriendsByKeyword(
            memberId, keyword, FriendshipStatus.ACCEPTED, pageable
        )

        return mapToFriendshipResponses(friendships)
    }

    override fun findRecentFriends(memberId: Long, pageable: Pageable): Page<FriendshipResponse> {
        val friendships = friendshipRepository.findRecentFriends(
            memberId, FriendshipStatus.ACCEPTED, pageable
        )

        return mapToFriendshipResponses(friendships)
    }

    override fun countPendingRequests(memberId: Long): Long {
        return friendshipRepository.countFriendshipsByRelationShipFriendMemberIdAndStatus(
            memberId,
            FriendshipStatus.PENDING
        )
    }

    override fun findPendingRequests(memberId: Long, pageable: Pageable): Page<Friendship> {
        return friendshipRepository.findReceivedFriendships(memberId, FriendshipStatus.PENDING, pageable)
    }

    private fun mapToFriendshipResponses(friendships: Page<Friendship>): Page<FriendshipResponse> {
        return friendships
            .map { friendship ->
                FriendshipResponse.from(
                    friendship,
                    friendship.relationShip.friendNickname,
                )
            }
    }
}
