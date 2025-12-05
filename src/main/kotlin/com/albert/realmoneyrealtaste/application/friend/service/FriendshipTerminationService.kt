package com.albert.realmoneyrealtaste.application.friend.service

import com.albert.realmoneyrealtaste.application.common.provided.DomainEventPublisher
import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest
import com.albert.realmoneyrealtaste.application.friend.exception.UnfriendException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipTerminator
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class FriendshipTerminationService(
    private val memberReader: MemberReader,
    private val domainEventPublisher: DomainEventPublisher,
    private val friendshipReader: FriendshipReader,
) : FriendshipTerminator {

    companion object {
        const val ERROR_UNFRIEND_FAILED = "친구 관계 해제에 실패했습니다."
    }

    override fun unfriend(request: UnfriendRequest) {
        try {
            // 요청자가 활성 회원인지 확인
            memberReader.readActiveMemberById(request.memberId)

            // 양방향 친구 관계 모두 해제
            val friendshipsToTerminate = mutableListOf<com.albert.realmoneyrealtaste.domain.friend.Friendship>()

            friendshipReader.findActiveFriendship(request.memberId, request.friendMemberId)
                ?.let { friendship ->
                    friendship.unfriend()
                    friendshipsToTerminate.add(friendship)
                }

            friendshipReader.findActiveFriendship(request.friendMemberId, request.memberId)
                ?.let { friendship ->
                    friendship.unfriend()
                    friendshipsToTerminate.add(friendship)
                }

            // 도메인 이벤트 발행
            friendshipsToTerminate.forEach { friendship ->
                domainEventPublisher.publishFrom(friendship)
            }
        } catch (e: IllegalArgumentException) {
            throw UnfriendException(ERROR_UNFRIEND_FAILED, e)
        }
    }
}
