package com.albert.realmoneyrealtaste.application.follow.service

import com.albert.realmoneyrealtaste.application.follow.dto.UnfollowRequest
import com.albert.realmoneyrealtaste.application.follow.event.UnfollowedEvent
import com.albert.realmoneyrealtaste.application.follow.exception.UnfollowException
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.follow.provided.FollowTerminator
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class FollowTerminationService(
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
    private val followReader: FollowReader,
) : FollowTerminator {

    companion object {
        const val ERROR_UNFOLLOW_FAILED = "언팔로우에 실패했습니다."
    }

    override fun unfollow(request: UnfollowRequest) {
        try {
            // 요청자가 활성 회원인지 확인
            memberReader.readActiveMemberById(request.followerId)

            // 팔로우 관계 조회
            val follow = followReader.findActiveFollow(request.followerId, request.followingId)

            // 언팔로우 처리
            follow.unfollow()

            // 이벤트 발행
            eventPublisher.publishEvent(
                UnfollowedEvent(
                    followId = follow.requireId(),
                    followerId = request.followerId,
                    followingId = request.followingId
                )
            )
        } catch (e: IllegalArgumentException) {
            throw UnfollowException(ERROR_UNFOLLOW_FAILED, e)
        }
    }
}
