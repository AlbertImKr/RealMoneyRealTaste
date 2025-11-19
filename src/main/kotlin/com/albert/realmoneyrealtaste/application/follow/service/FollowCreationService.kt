package com.albert.realmoneyrealtaste.application.follow.service

import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.event.FollowStartedEvent
import com.albert.realmoneyrealtaste.application.follow.exception.FollowCreateException
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class FollowCreationService(
    private val followRepository: FollowRepository,
    private val memberReader: MemberReader,
    private val eventPublisher: ApplicationEventPublisher,
    private val followReader: FollowReader,
) : FollowCreator {

    companion object {
        const val ERROR_FOLLOW_FAILED = "팔로우에 실패했습니다."
    }

    override fun follow(request: FollowCreateRequest): Follow {
        try {
            // 팔로워와 팔로잉 대상이 모두 활성 회원인지 확인
            val follower = memberReader.readActiveMemberById(request.followerId)
            val following = memberReader.readActiveMemberById(request.followingId)

            // 기존 팔로우 관계 확인
            val existingFollow = followReader.findFollowByRelationship(request.followerId, request.followingId)

            if (existingFollow != null) {
                existingFollow.reactivate()
                return existingFollow
            }

            // 팔로우 관계 생성
            val command = FollowCreateCommand(
                followerId = follower.requireId(),
                followerNickname = follower.nickname.value,
                followingId = following.requireId(),
                followingNickname = following.nickname.value,
            )
            val follow = Follow.create(command)
            val savedFollow = followRepository.save(follow)

            // 이벤트 발행
            eventPublisher.publishEvent(
                FollowStartedEvent(
                    followId = savedFollow.requireId(),
                    followerId = command.followerId,
                    followingId = command.followingId
                )
            )

            return savedFollow
        } catch (e: IllegalArgumentException) {
            throw FollowCreateException(ERROR_FOLLOW_FAILED, e)
        }
    }
}
