package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.friend.exception.FriendRequestException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class FriendRequestService(
    private val friendshipReader: FriendshipReader,
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
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
                fromMemberNickName = fromMember.nickname.value,
                toMemberId = toMemberId,
                toMemberNickname = toMember.nickname.value
            )
            // 요청자와 대상자가 모두 활성 회원인지 확인
            validateMembersExist(command)

            // 기존 친구 관계나 요청이 있는지 확인
            val existingFriendship = friendshipReader.findByMembersId(command.fromMemberId, command.toMemberId)

            if (existingFriendship != null) {
                existingFriendship.status = FriendshipStatus.PENDING
                publishEvent(existingFriendship, command)
                return existingFriendship
            }

            // 친구 요청 생성
            val friendship = Friendship.request(command)
            friendshipRepository.save(friendship)

            // 이벤트 발행 (알림 등을 위해)
            publishEvent(friendship, command)

            return friendship
        } catch (e: IllegalArgumentException) {
            throw FriendRequestException(ERROR_FRIEND_REQUEST_FAILED, e)
        }
    }

    private fun validateMembersExist(command: FriendRequestCommand) {
        memberReader.readActiveMemberById(command.fromMemberId)
        memberReader.readActiveMemberById(command.toMemberId)
    }

    private fun publishEvent(
        friendship: Friendship,
        command: FriendRequestCommand,
    ) {
        eventPublisher.publishEvent(
            FriendRequestSentEvent(
                friendshipId = friendship.requireId(),
                fromMemberId = command.fromMemberId,
                toMemberId = command.toMemberId
            )
        )
    }
}
