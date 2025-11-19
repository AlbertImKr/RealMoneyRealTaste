package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.event.FollowStartedEvent
import com.albert.realmoneyrealtaste.application.follow.exception.FollowCreateException
import com.albert.realmoneyrealtaste.application.follow.required.FollowRepository
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@RecordApplicationEvents
class FollowCreatorTest(
    private val followCreator: FollowCreator,
    private val followRepository: FollowRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `follow - success - creates and saves follow with event publication`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "follower@test.com",
            nickname = "follower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "following@test.com",
            nickname = "following"
        )
        val request = FollowCreateRequest(
            followerId = follower.requireId(),
            followingId = following.requireId(),
        )
        applicationEvents.clear()

        val result = followCreator.follow(request)

        // 반환된 결과 검증
        assertAll(
            { assertNotNull(result.id) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) },
            { assertEquals(FollowStatus.ACTIVE, result.status) },
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) },
            { assertEquals(result.createdAt, result.updatedAt) }
        )

        // 데이터베이스에 저장 확인
        val saved = followRepository.findById(result.requireId())
        assertAll(
            { assertNotNull(saved) },
            { assertEquals(follower.requireId(), saved?.relationship?.followerId) },
            { assertEquals(following.requireId(), saved?.relationship?.followingId) },
            { assertEquals(FollowStatus.ACTIVE, saved?.status) }
        )

        // 이벤트 발행 확인
        val events = applicationEvents.stream(FollowStartedEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(result.requireId(), event.followId) },
            { assertEquals(follower.requireId(), event.followerId) },
            { assertEquals(following.requireId(), event.followingId) }
        )
    }

    @Test
    fun `follow - failure - throws exception when follower does not exist`() {
        val nonExistentFollowerId = 999999L
        val following = testMemberHelper.createActivatedMember(
            email = "existing@test.com",
            nickname = "existing"
        )
        val request = FollowCreateRequest(
            followerId = nonExistentFollowerId,
            followingId = following.requireId(),
        )

        assertFailsWith<FollowCreateException> {
            followCreator.follow(request)
        }.let {
            assertEquals("팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `follow - failure - throws exception when following target does not exist`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "existing2@test.com",
            nickname = "existing2"
        )
        val nonExistentFollowingId = 999999L
        val request = FollowCreateRequest(
            followerId = follower.requireId(),
            followingId = nonExistentFollowingId,
        )

        assertFailsWith<FollowCreateException> {
            followCreator.follow(request = request)
        }.let {
            assertEquals("팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `follow - failure - throws exception when duplicate active follow exists`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "duplicate-follower@test.com",
            nickname = "duplicatefollower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "duplicate-following@test.com",
            nickname = "duplicatefollowing"
        )

        // 첫 번째 팔로우
        val request = FollowCreateRequest(
            followerId = follower.requireId(),
            followingId = following.requireId()
        )
        followCreator.follow(request)

        // 중복 팔로우 시도
        assertFailsWith<FollowCreateException> {
            followCreator.follow(request)
        }.let {
            assertEquals("팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `follow - success - allows follow after previous unfollow`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "refollow-follower@test.com",
            nickname = "refollowfollower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "refollow-following@test.com",
            nickname = "refollowfollowing"
        )

        // 첫 번째 팔로우 후 언팔로우
        val request = FollowCreateRequest(
            followerId = follower.requireId(),
            followingId = following.requireId()
        )
        val firstFollow = followCreator.follow(request)
        firstFollow.unfollow()

        // 다시 팔로우 (허용되어야 함)
        val result = followCreator.follow(request)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) },
            { assertEquals(FollowStatus.ACTIVE, result.status) },
        )
    }

    @Test
    fun `follow - success - allows follow after previous block`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "block-follower@test.com",
            nickname = "blockfollower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "block-following@test.com",
            nickname = "blockfollowing"
        )

        // 첫 번째 팔로우 후 차단
        val request = FollowCreateRequest(
            followerId = follower.requireId(),
            followingId = following.requireId()
        )
        val firstFollow = followCreator.follow(request)
        firstFollow.block()
        followRepository.save(firstFollow)

        // 다시 팔로우 (허용되어야 함)
        val result = followCreator.follow(request)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(follower.requireId(), result.relationship.followerId) },
            { assertEquals(following.requireId(), result.relationship.followingId) },
            { assertEquals(FollowStatus.ACTIVE, result.status) }
        )
    }

    @Test
    fun `follow - success - handles multiple follow relationships correctly`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "multi-follower@test.com",
            nickname = "multifollower"
        )
        val following1 = testMemberHelper.createActivatedMember(
            email = "following1@test.com",
            nickname = "following1"
        )
        val following2 = testMemberHelper.createActivatedMember(
            email = "following2@test.com",
            nickname = "following2"
        )

        val request1 = FollowCreateRequest(
            follower.requireId(),
            following1.requireId()
        )
        val request2 = FollowCreateRequest(
            follower.requireId(),
            following2.requireId()
        )
        val result1 = followCreator.follow(request1)
        val result2 = followCreator.follow(request2)

        assertAll(
            { assertEquals(follower.requireId(), result1.relationship.followerId) },
            { assertEquals(following1.requireId(), result1.relationship.followingId) },
            { assertEquals(follower.requireId(), result2.relationship.followerId) },
            { assertEquals(following2.requireId(), result2.relationship.followingId) },
            { assertNotEquals(result1.requireId(), result2.requireId()) }
        )
    }

    @Test
    fun `follow - success - validates member status is active`() {
        val activeFollower = testMemberHelper.createActivatedMember(
            email = "active-follower@test.com",
            nickname = "activefollower"
        )

        // 비활성 회원 생성 시도 시 MemberReader.readActiveMemberById에서 예외 발생
        val inactiveMember = testMemberHelper.createMember(
            email = "inactive@test.com",
            nickname = "inactive"
        )
        // 활성화하지 않음 (PENDING 상태)

        val request = FollowCreateRequest(
            activeFollower.requireId(),
            inactiveMember.requireId()
        )

        assertFailsWith<FollowCreateException> {
            followCreator.follow(request)
        }.let {
            assertEquals("팔로우에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `follow - failure - throws exception when following self`() {
        val member = testMemberHelper.createActivatedMember(
            email = "self@test.com",
            nickname = "self"
        )

        assertFailsWith<IllegalArgumentException> {
            val request = FollowCreateRequest(
                member.requireId(),
                member.requireId(),
            )
            followCreator.follow(request)
        }
    }

    @Test
    fun `follow - success - persists follow correctly`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "persist-follower@test.com",
            nickname = "persistfollower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "persist-following@test.com",
            nickname = "persistfollowing"
        )

        val request = FollowCreateRequest(
            follower.requireId(),
            following.requireId(),
        )
        val result = followCreator.follow(request)

        // 데이터베이스에서 다시 조회하여 확인
        val persisted = followRepository.findById(result.requireId())
        assertAll(
            { assertNotNull(persisted) },
            { assertEquals(result.relationship.followerId, persisted?.relationship?.followerId) },
            { assertEquals(result.relationship.followingId, persisted?.relationship?.followingId) },
            { assertEquals(result.status, persisted?.status) },
            { assertEquals(result.createdAt, persisted?.createdAt) },
            { assertEquals(result.updatedAt, persisted?.updatedAt) }
        )
    }

    @Test
    fun `follow - success - creates follow with correct initial state`() {
        val follower = testMemberHelper.createActivatedMember(
            email = "state-follower@test.com",
            nickname = "statefollower"
        )
        val following = testMemberHelper.createActivatedMember(
            email = "state-following@test.com",
            nickname = "statefollowing"
        )

        val request = FollowCreateRequest(
            follower.requireId(),
            following.requireId(),
        )
        val result = followCreator.follow(request)

        assertAll(
            { assertEquals(FollowStatus.ACTIVE, result.status) },
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) },
            { assertEquals(result.createdAt, result.updatedAt) } // 초기 생성 시 같아야 함
        )
    }

    @Test
    fun `follow - success - handles bidirectional follow correctly`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "bidirect1@test.com",
            nickname = "bidirect1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "bidirect2@test.com",
            nickname = "bidirect2"
        )

        // A가 B를 팔로우
        val request1 = FollowCreateRequest(
            member1.requireId(),
            member2.requireId()
        )
        val result1 = followCreator.follow(request1)

        // B가 A를 팔로우 (허용되어야 함)
        val request2 = FollowCreateRequest(member2.requireId(), member1.requireId())
        val result2 = followCreator.follow(request2)

        assertAll(
            { assertEquals(member1.requireId(), result1.relationship.followerId) },
            { assertEquals(member2.requireId(), result1.relationship.followingId) },
            { assertEquals(member2.requireId(), result2.relationship.followerId) },
            { assertEquals(member1.requireId(), result2.relationship.followingId) },
            { assertNotEquals(result1.requireId(), result2.requireId()) }
        )
    }
}
