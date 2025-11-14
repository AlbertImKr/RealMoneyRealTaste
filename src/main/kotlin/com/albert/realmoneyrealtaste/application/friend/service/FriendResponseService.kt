package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.exception.FriendResponseException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.application.friend.provided.FriendResponder
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class FriendResponseService(
    private val memberReader: MemberReader,
    private val friendshipReader: FriendshipReader,
    private val eventPublisher: ApplicationEventPublisher,
    private val friendRequestor: FriendRequestor,
) : FriendResponder {

    companion object {
        const val ERROR_FRIEND_RESPONSE_FAILED = "친구 요청 응답에 실패했습니다."
        const val ERROR_NOT_AUTHORIZED = "이 친구 요청에 응답할 권한이 없습니다."
    }

    override fun respondToFriendRequest(request: FriendResponseRequest): Friendship {
        try {
            // 응답자가 활성 회원인지 확인
            memberReader.readActiveMemberById(request.respondentMemberId)

            // 친구 요청 조회
            val friendship = friendshipReader.findFriendshipById(request.friendshipId)

            // 응답 권한 확인 (요청을 받은 사람만 응답 가능)
            require(friendship.isReceivedBy(request.respondentMemberId)) { ERROR_NOT_AUTHORIZED }

            // 친구 요청 응답 처리
            if (request.accept) {
                friendship.accept()
                // 양방향 친구 관계 생성
                createReverseFriendship(friendship)
            } else {
                friendship.reject()
            }

            // 이벤트 발행
            publishEvent(friendship, request.accept)

            return friendship
        } catch (e: IllegalArgumentException) {
            throw FriendResponseException(ERROR_FRIEND_RESPONSE_FAILED, e)
        }
    }

    private fun publishEvent(
        friendship: Friendship,
        accept: Boolean,
    ) {
        val event = if (accept) {
            FriendRequestAcceptedEvent(
                friendshipId = friendship.requireId(),
                fromMemberId = friendship.relationShip.memberId,
                toMemberId = friendship.relationShip.friendMemberId
            )
        } else {
            FriendRequestRejectedEvent(
                friendshipId = friendship.requireId(),
                fromMemberId = friendship.relationShip.memberId,
                toMemberId = friendship.relationShip.friendMemberId
            )
        }
        eventPublisher.publishEvent(event)
    }

    private fun createReverseFriendship(originalFriendship: Friendship) {
        val reverseFriendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = originalFriendship.relationShip.friendMemberId,
                toMemberId = originalFriendship.relationShip.memberId
            )
        )
        reverseFriendship.accept() // 즉시 수락 상태로 설정
    }
}
