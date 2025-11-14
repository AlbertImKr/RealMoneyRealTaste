package com.albert.realmoneyrealtaste.application.follow.dto

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FollowResponseTest {

    @Test
    fun `from - success - creates response with all follow information`() {
        val followerId = 1L
        val followingId = 2L
        val followerNickname = "follower"
        val followingNickname = "following"
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)

        // ID 설정 (실제로는 JPA가 수행)
        setFollowId(follow, 100L)

        val result = FollowResponse.from(follow, followerNickname, followingNickname)

        assertAll(
            { assertEquals(100L, result.followId) },
            { assertEquals(followerId, result.followerId) },
            { assertEquals(followerNickname, result.followerNickname) },
            { assertEquals(followingId, result.followingId) },
            { assertEquals(followingNickname, result.followingNickname) },
            { assertEquals(FollowStatus.ACTIVE, result.status) },
            { assertEquals(follow.createdAt, result.createdAt) },
            { assertEquals(follow.updatedAt, result.updatedAt) }
        )
    }

    @Test
    fun `from - success - handles unfollowed status correctly`() {
        val followerId = 10L
        val followingId = 20L
        val followerNickname = "unfollower"
        val followingNickname = "unfollowed"
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        follow.unfollow()
        setFollowId(follow, 200L)

        val result = FollowResponse.from(follow, followerNickname, followingNickname)

        assertAll(
            { assertEquals(200L, result.followId) },
            { assertEquals(followerId, result.followerId) },
            { assertEquals(followerNickname, result.followerNickname) },
            { assertEquals(followingId, result.followingId) },
            { assertEquals(followingNickname, result.followingNickname) },
            { assertEquals(FollowStatus.UNFOLLOWED, result.status) },
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) }
        )
    }

    @Test
    fun `from - success - handles blocked status correctly`() {
        val followerId = 30L
        val followingId = 40L
        val followerNickname = "blocker"
        val followingNickname = "blocked"
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        follow.block()
        setFollowId(follow, 300L)

        val result = FollowResponse.from(follow, followerNickname, followingNickname)

        assertAll(
            { assertEquals(300L, result.followId) },
            { assertEquals(FollowStatus.BLOCKED, result.status) },
            { assertEquals(followerNickname, result.followerNickname) },
            { assertEquals(followingNickname, result.followingNickname) }
        )
    }

    @Test
    fun `from - success - preserves exact timestamp values`() {
        val followerId = 70L
        val followingId = 80L
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        setFollowId(follow, 500L)

        val originalCreatedAt = follow.createdAt
        val originalUpdatedAt = follow.updatedAt

        val result = FollowResponse.from(follow, "nick1", "nick2")

        assertAll(
            { assertEquals(originalCreatedAt, result.createdAt) },
            { assertEquals(originalUpdatedAt, result.updatedAt) }
        )
    }

    @Test
    fun `from - success - handles empty nicknames`() {
        val followerId = 90L
        val followingId = 100L
        val emptyNickname = ""
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        setFollowId(follow, 600L)

        val result = FollowResponse.from(follow, emptyNickname, emptyNickname)

        assertAll(
            { assertEquals(emptyNickname, result.followerNickname) },
            { assertEquals(emptyNickname, result.followingNickname) }
        )
    }

    @Test
    fun `from - success - handles special characters in nicknames`() {
        val followerId = 110L
        val followingId = 120L
        val specialNickname1 = "팔로워🎉"
        val specialNickname2 = "following@#$%"
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        setFollowId(follow, 700L)

        val result = FollowResponse.from(follow, specialNickname1, specialNickname2)

        assertAll(
            { assertEquals(specialNickname1, result.followerNickname) },
            { assertEquals(specialNickname2, result.followingNickname) }
        )
    }

    @Test
    fun `from - success - handles large member IDs`() {
        val largeFollowerId = Long.MAX_VALUE - 1
        val largeFollowingId = Long.MAX_VALUE
        val command = FollowCreateCommand(largeFollowerId, largeFollowingId)
        val follow = Follow.create(command)
        setFollowId(follow, Long.MAX_VALUE)

        val result = FollowResponse.from(follow, "large1", "large2")

        assertAll(
            { assertEquals(Long.MAX_VALUE, result.followId) },
            { assertEquals(largeFollowerId, result.followerId) },
            { assertEquals(largeFollowingId, result.followingId) }
        )
    }

    @Test
    fun `from - success - handles long nicknames`() {
        val followerId = 130L
        val followingId = 140L
        val longNickname1 = "a".repeat(100)
        val longNickname2 = "b".repeat(100)
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        setFollowId(follow, 800L)

        val result = FollowResponse.from(follow, longNickname1, longNickname2)

        assertAll(
            { assertEquals(longNickname1, result.followerNickname) },
            { assertEquals(longNickname2, result.followingNickname) },
            { assertEquals(100, result.followerNickname.length) },
            { assertEquals(100, result.followingNickname.length) }
        )
    }

    @Test
    fun `from - success - handles status transitions correctly`() {
        val followerId = 150L
        val followingId = 160L
        val command = FollowCreateCommand(followerId, followingId)
        val follow = Follow.create(command)
        setFollowId(follow, 900L)

        // 초기 ACTIVE 상태
        val activeResponse = FollowResponse.from(follow, "follower", "following")
        assertEquals(FollowStatus.ACTIVE, activeResponse.status)

        // UNFOLLOWED 상태로 변경
        follow.unfollow()
        val unfollowedResponse = FollowResponse.from(follow, "follower", "following")
        assertEquals(FollowStatus.UNFOLLOWED, unfollowedResponse.status)

        // ACTIVE 상태로 재활성화
        follow.reactivate()
        val reactivatedResponse = FollowResponse.from(follow, "follower", "following")
        assertEquals(FollowStatus.ACTIVE, reactivatedResponse.status)

        // BLOCKED 상태로 변경
        follow.block()
        val blockedResponse = FollowResponse.from(follow, "follower", "following")
        assertEquals(FollowStatus.BLOCKED, blockedResponse.status)
    }

    /**
     * 리플렉션을 사용하여 Follow의 ID 설정
     * (테스트 목적으로만 사용)
     */
    private fun setFollowId(follow: Follow, id: Long) {
        val idField = follow.javaClass.superclass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(follow, id)
    }
}
