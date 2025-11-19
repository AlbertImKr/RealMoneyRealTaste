package com.albert.realmoneyrealtaste.domain.follow

import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FollowTest {

    @Test
    fun `create - success - creates active follow with valid command`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followingId = 2L
        val followingNickname = "following"
        val command = FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        val before = LocalDateTime.now()

        val follow = Follow.create(command)

        assertAll(
            { assertEquals(followerId, follow.relationship.followerId) },
            { assertEquals(followerNickname, follow.relationship.followerNickname) },
            { assertEquals(followingId, follow.relationship.followingId) },
            { assertEquals(followingNickname, follow.relationship.followingNickname) },
            { assertEquals(FollowStatus.ACTIVE, follow.status) },
            { assertTrue(follow.createdAt >= before) },
            { assertTrue(follow.updatedAt >= before) },
            { assertEquals(follow.createdAt, follow.updatedAt) }
        )
    }

    @Test
    fun `unfollow - success - changes status to unfollowed when active`() {
        val follow = createActiveFollow()
        val beforeUpdate = follow.updatedAt

        follow.unfollow()

        assertAll(
            { assertEquals(FollowStatus.UNFOLLOWED, follow.status) },
            { assertTrue(follow.updatedAt >= beforeUpdate) }
        )
    }

    @Test
    fun `unfollow - failure - throws exception when status is not active`() {
        val follow = createActiveFollow()
        follow.unfollow() // 이미 언팔로우된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            follow.unfollow()
        }.let {
            assertEquals(Follow.ERROR_CANNOT_UNFOLLOW_INACTIVE, it.message)
        }
    }

    @Test
    fun `unfollow - failure - throws exception when status is blocked`() {
        val follow = createActiveFollow()
        follow.block() // 차단된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            follow.unfollow()
        }.let {
            assertEquals(Follow.ERROR_CANNOT_UNFOLLOW_INACTIVE, it.message)
        }
    }

    @Test
    fun `reactivate - success - changes status to active when unfollowed`() {
        val follow = createActiveFollow()
        follow.unfollow() // 먼저 언팔로우
        val beforeReactivate = follow.updatedAt

        follow.reactivate()

        assertAll(
            { assertEquals(FollowStatus.ACTIVE, follow.status) },
            { assertTrue(follow.updatedAt >= beforeReactivate) }
        )
    }

    @Test
    fun `reactivate - success - changes status to active when blocked`() {
        val follow = createActiveFollow()
        follow.block() // 먼저 차단
        val beforeReactivate = follow.updatedAt

        follow.reactivate()

        assertAll(
            { assertEquals(FollowStatus.ACTIVE, follow.status) },
            { assertTrue(follow.updatedAt >= beforeReactivate) }
        )
    }

    @Test
    fun `reactivate - failure - throws exception when status is already active`() {
        val follow = createActiveFollow()

        assertFailsWith<IllegalArgumentException> {
            follow.reactivate()
        }.let {
            assertEquals(Follow.ERROR_CANNOT_REACTIVATE_ACTIVE, it.message)
        }
    }

    @Test
    fun `block - success - changes status to blocked from any status`() {
        val follow = createActiveFollow()
        val beforeBlock = follow.updatedAt

        follow.block()

        assertAll(
            { assertEquals(FollowStatus.BLOCKED, follow.status) },
            { assertTrue(follow.updatedAt >= beforeBlock) }
        )
    }

    @Test
    fun `block - success - changes status to blocked from unfollowed status`() {
        val follow = createActiveFollow()
        follow.unfollow() // 먼저 언팔로우
        val beforeBlock = follow.updatedAt

        follow.block()

        assertAll(
            { assertEquals(FollowStatus.BLOCKED, follow.status) },
            { assertTrue(follow.updatedAt >= beforeBlock) }
        )
    }

    @Test
    fun `isFollowedBy - success - returns true when member is follower and status is active`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)

        val result = follow.isFollowedBy(followerId)

        assertTrue(result)
    }

    @Test
    fun `isFollowedBy - success - returns false when member is not follower`() {
        val followerId = 1L
        val followingId = 2L
        val otherId = 3L
        val follow = createFollow(followerId, followingId)

        val result = follow.isFollowedBy(otherId)

        assertFalse(result)
    }

    @Test
    fun `isFollowedBy - success - returns false when member is follower but status is not active`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)
        follow.unfollow() // 비활성화

        val result = follow.isFollowedBy(followerId)

        assertFalse(result)
    }

    @Test
    fun `isFollowing - success - returns true when member is following and status is active`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)

        val result = follow.isFollowing(followingId)

        assertTrue(result)
    }

    @Test
    fun `isFollowing - success - returns false when member is not following`() {
        val followerId = 1L
        val followingId = 2L
        val otherId = 3L
        val follow = createFollow(followerId, followingId)

        val result = follow.isFollowing(otherId)

        assertFalse(result)
    }

    @Test
    fun `isFollowing - success - returns false when member is following but status is not active`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)
        follow.block() // 차단됨

        val result = follow.isFollowing(followingId)

        assertFalse(result)
    }

    @Test
    fun `isRelatedTo - success - returns true when member is follower`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)

        val result = follow.isRelatedTo(followerId)

        assertTrue(result)
    }

    @Test
    fun `isRelatedTo - success - returns true when member is following`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)

        val result = follow.isRelatedTo(followingId)

        assertTrue(result)
    }

    @Test
    fun `isRelatedTo - success - returns false when member is not related`() {
        val followerId = 1L
        val followingId = 2L
        val otherId = 3L
        val follow = createFollow(followerId, followingId)

        val result = follow.isRelatedTo(otherId)

        assertFalse(result)
    }

    @Test
    fun `isRelatedTo - success - returns true regardless of status`() {
        val followerId = 1L
        val followingId = 2L
        val follow = createFollow(followerId, followingId)
        follow.unfollow() // 비활성화

        assertAll(
            { assertTrue(follow.isRelatedTo(followerId)) },
            { assertTrue(follow.isRelatedTo(followingId)) }
        )
    }

    private fun createActiveFollow(): Follow {
        val command = FollowCreateCommand(1L, "follower", 2L, "following")
        return Follow.create(command)
    }

    private fun createFollow(followerId: Long, followingId: Long): Follow {
        val command = FollowCreateCommand(followerId, "follower", followingId, "following")
        return Follow.create(command)
    }
}
