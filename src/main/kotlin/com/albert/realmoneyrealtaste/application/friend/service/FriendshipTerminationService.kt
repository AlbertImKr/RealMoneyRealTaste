package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest
import com.albert.realmoneyrealtaste.application.friend.exception.UnfriendException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipTerminator
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class FriendshipTerminationService(
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
    private val friendshipReader: FriendshipReader,
) : FriendshipTerminator {

    companion object {
        const val ERROR_UNFRIEND_FAILED = "친구 관계 해제에 실패했습니다."
    }

    override fun unfriend(request: UnfriendRequest) {
        try {
            // 요청자가 활성 회원인지 확인
            memberReader.readActiveMemberById(request.memberId)

            // 친구 관계 조회
            friendshipReader.findActiveFriendship(request.memberId, request.friendMemberId)
                ?.unfriend()

            // 양방향 친구 관계 모두 해제
            friendshipReader.findActiveFriendship(request.friendMemberId, request.memberId)
                ?.unfriend()

            // 이벤트 발행
            publishEvent(request)
        } catch (e: IllegalArgumentException) {
            throw UnfriendException(ERROR_UNFRIEND_FAILED, e)
        }
    }

    private fun publishEvent(request: UnfriendRequest) {
        eventPublisher.publishEvent(
            FriendshipTerminatedEvent(
                memberId = request.memberId,
                friendMemberId = request.friendMemberId
            )
        )
    }
}
