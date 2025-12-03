package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.event.UnfollowedEvent
import com.albert.realmoneyrealtaste.application.follow.exception.UnfollowException
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RecordApplicationEvents
class FollowTerminatorTest(
    private val followTerminator: FollowTerminator,
    private val followCreator: FollowCreator,
    private val followRepository: FollowRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `unfollow - success - terminates follow and publishes event`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "unfollow-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "unfollow-following@test.com",
            nickname = "following"
        )

        // 팔로우 관계 생성
        val follow = createActiveFollow(follower.requireId(), following.requireId())
        applicationEvents.clear()

        // 언팔로우
        followTerminator.unfollow(
            followerId = follower.requireId(),
            followingId = following.requireId()
        )

        // 팔로우 관계 상태 확인
        val updatedFollow = followRepository.findById(follow.requireId())
        assertAll(
            { assertNotNull(updatedFollow) },
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow?.status) },
            { assertNotNull(updatedFollow?.updatedAt) }
        )

        // 이벤트 발행 확인
        val events = applicationEvents.stream(UnfollowedEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(follow.requireId(), event.followId) },
            { assertEquals(follower.requireId(), event.followerId) },
            { assertEquals(following.requireId(), event.followingId) }
        )
    }

    @Test
    fun `unfollow - failure - throws exception when follower does not exist`() {
        val nonExistentFollowerId = 999999L
        val following = testMemberHelper.createActivatedMember(
            email = "existing@test.com",
            nickname = "existing"
        )


        assertFailsWith<UnfollowException> {
            followTerminator.unfollow(
                followerId = nonExistentFollowerId,
                followingId = following.requireId()
            )
        }.let {
            assertEquals("언팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfollow - failure - throws exception when active follow does not exist`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "nofollow-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "nofollow-following@test.com",
            nickname = "following"
        )

        assertFailsWith<UnfollowException> {
            followTerminator.unfollow(
                followerId = follower.requireId(),
                followingId = following.requireId()
            )
        }.let {
            assertEquals("언팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfollow - failure - throws exception when follow is already unfollowed`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "already-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "already-following@test.com",
            nickname = "following"
        )

        // 팔로우 후 이미 언팔로우
        val follow = createActiveFollow(follower.requireId(), following.requireId())
        follow.unfollow()
        followRepository.save(follow)

        assertFailsWith<UnfollowException> {
            followTerminator.unfollow(
                followerId = follower.requireId(),
                followingId = following.requireId()
            )
        }.let {
            assertEquals("언팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfollow - failure - throws exception when follow is blocked`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "blocked-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "blocked-following@test.com",
            nickname = "following"
        )

        // 팔로우 후 차단
        val follow = createActiveFollow(follower.requireId(), following.requireId())
        follow.block()
        followRepository.save(follow)

        assertFailsWith<UnfollowException> {
            followTerminator.unfollow(
                followerId = follower.requireId(),
                followingId = following.requireId()
            )
        }.let {
            assertEquals("언팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfollow - failure - throws exception when follower is not active`() {
        val activeFollowing = testMemberHelper.createActivatedMember(
            email = "active-following@test.com",
            nickname = "following"
        )
        val inactiveFollower = testMemberHelper.createMember(
            email = "inactive-follower@test.com",
            nickname = "follower"
        )

        assertFailsWith<UnfollowException> {
            followTerminator.unfollow(
                followerId = inactiveFollower.requireId(),
                followingId = activeFollowing.requireId()
            )
        }.let {
            assertEquals("언팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfollow - success - updates follow status and timestamp correctly`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "timestamp-follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "timestamp-following@test.com",
            nickname = "following"
        )

        val follow = createActiveFollow(follower.requireId(), following.requireId())
        val originalUpdatedAt = follow.updatedAt

        followTerminator.unfollow(
            followerId = follower.requireId(),
            followingId = following.requireId()
        )

        val updatedFollow = followRepository.findById(follow.requireId())!!
        assertAll(
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow.status) },
            { assertEquals(follow.createdAt, updatedFollow.createdAt) }, // 생성일은 변경되지 않음
            { kotlin.test.assertTrue(updatedFollow.updatedAt.isAfter(originalUpdatedAt)) } // 업데이트 일시는 변경됨
        )
    }

    @Test
    fun `unfollow - success - handles multiple unfollows correctly`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "multi-follower@test.com",
            nickname = "follower"
        )
        val following1 = testMemberHelper.createActivatedMember(
            email = "following1@test.com",
            nickname = "following1"
        )
        val following2 = testMemberHelper.createActivatedMember(
            email = "following2@test.com",
            nickname = "following2"
        )

        // 두 개의 팔로우 관계 생성
        val follow1 = createActiveFollow(follower.requireId(), following1.requireId())
        val follow2 = createActiveFollow(follower.requireId(), following2.requireId())
        applicationEvents.clear()

        // 첫 번째 언팔로우
        followTerminator.unfollow(
            followerId = follower.requireId(),
            followingId = following1.requireId(),
        )

        // 두 번째 언팔로우
        followTerminator.unfollow(
            followerId = follower.requireId(),
            followingId = following2.requireId(),
        )

        // 두 팔로우 모두 언팔로우 상태 확인
        val updatedFollow1 = followRepository.findById(follow1.requireId())!!
        val updatedFollow2 = followRepository.findById(follow2.requireId())!!
        assertAll(
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow1.status) },
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow2.status) }
        )

        // 이벤트 두 번 발행 확인
        val events = applicationEvents.stream(UnfollowedEvent::class.java).toList()
        assertEquals(2, events.size)
    }

    @Test
    fun `unfollow - success - does not affect other follow relationships`() {
        val follower1 = testMemberHelper.createActivatedMember(
            email = "follower1@test.com",
            nickname = "follower1"
        )
        val follower2 = testMemberHelper.createActivatedMember(
            email = "follower2@test.com",
            nickname = "follower2"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "common-following@test.com",
            nickname = "following"
        )

        // 두 명이 같은 사람을 팔로우
        val follow1 = createActiveFollow(follower1.requireId(), following.requireId())
        val follow2 = createActiveFollow(follower2.requireId(), following.requireId())

        // 첫 번째 팔로워만 언팔로우
        followTerminator.unfollow(
            followerId = follower1.requireId(),
            followingId = following.requireId(),
        )

        // 첫 번째는 언팔로우, 두 번째는 여전히 활성 상태
        val updatedFollow1 = followRepository.findById(follow1.requireId())!!
        val updatedFollow2 = followRepository.findById(follow2.requireId())!!
        assertAll(
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow1.status) },
            { assertEquals(FollowStatus.ACTIVE, updatedFollow2.status) }
        )
    }

    @Test
    fun `unfollow - success - handles bidirectional unfollow independently`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "bidirect1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "bidirect2@test.com",
            nickname = "member2"
        )

        // 서로 팔로우
        val follow1to2 = createActiveFollow(member1.requireId(), member2.requireId())
        val follow2to1 = createActiveFollow(member2.requireId(), member1.requireId())

        // member1이 member2를 언팔로우
        followTerminator.unfollow(
            followerId = member1.requireId(),
            followingId = member2.requireId(),
        )

        // member1->member2는 언팔로우, member2->member1은 여전히 활성
        val updatedFollow1to2 = followRepository.findById(follow1to2.requireId())!!
        val updatedFollow2to1 = followRepository.findById(follow2to1.requireId())!!
        assertAll(
            { assertEquals(FollowStatus.UNFOLLOWED, updatedFollow1to2.status) },
            { assertEquals(FollowStatus.ACTIVE, updatedFollow2to1.status) }
        )
    }

    private fun createActiveFollow(followerId: Long, followingId: Long): Follow {
        val request = FollowCreateRequest(followerId, followingId)
        return followCreator.follow(request)
    }
}
