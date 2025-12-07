package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.friend.exception.FriendRequestException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class FriendRequestService(
    private val friendshipReader: FriendshipReader,
    private val memberReader: MemberReader,
    private val domainEventPublisher: DomainEventPublisher,
    private val friendshipRepository: FriendshipRepository,
) : FriendRequestor {

    companion object {
        const val ERROR_FRIEND_REQUEST_FAILED = "친구 요청에 실패했습니다."
    }

    override fun sendFriendRequest(fromMemberId: Long, toMemberId: Long): Friendship {
        try {
            val fromMember = memberReader.readActiveMemberById(fromMemberId)
            val toMember = memberReader.readActiveMemberById(toMemberId)

            val command = FriendRequestCommand(
                fromMemberId = fromMemberId,
                fromMemberNickname = fromMember.nickname.value,
                fromMemberProfileImageId = fromMember.profileImageId,
                toMemberId = toMemberId,
                toMemberNickname = toMember.nickname.value,
                toMemberProfileImageId = toMember.profileImageId,
            )

            // 기존 친구 관계나 요청이 있는지 확인
            val existingFriendship = friendshipReader.findByMembersId(command.fromMemberId, command.toMemberId)

            if (existingFriendship != null) {
                existingFriendship.rePending()
                // 도메인 이벤트 발행
                domainEventPublisher.publishFrom(existingFriendship)
                return existingFriendship
            }

            // 친구 요청 생성
            val friendship = Friendship.request(command)
            val savedFriendship = friendshipRepository.save(friendship)

            // 도메인 이벤트 발행
            domainEventPublisher.publishFrom(savedFriendship)

            return savedFriendship
        } catch (e: IllegalArgumentException) {
            throw FriendRequestException(ERROR_FRIEND_REQUEST_FAILED, e)
        }
    }
}
