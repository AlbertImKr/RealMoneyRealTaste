package com.albert.realmoneyrealtaste.domain.follow.command

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FollowCreateCommandTest {

    @Test
    fun `construct - success - creates command with valid ids`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followingId = 2L
        val followingNickname = "following"

        val command = FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) }
        )
    }

    @Test
    fun `construct - failure - throws exception when followerId is zero`() {
        val followerId = 0L
        val followerNickname = "follower"
        val followingId = 2L
        val followingNickname = "following"

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followingId = 0L
        val followingNickname = "following"

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals("팔로잉 대상 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when following self`() {
        val memberId = 1L
        val nickname = "test"

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(memberId, nickname, memberId, nickname)
        }.let {
            assertEquals("자기 자신을 팔로우할 수 없습니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerNickname is blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followingId = 2L
        val followingNickname = "following"

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "   "
        val followingId = 2L
        val followingNickname = "following"

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingNickname is blank`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followingId = 2L
        val followingNickname = ""

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followingId = 2L
        val followingNickname = "   "

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both nicknames are blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followingId = 2L
        val followingNickname = ""

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)
        }.let {
            // followerNickname 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - success - accepts valid nicknames with special characters`() {
        val followerId = 1L
        val followerNickname = "follower_123"
        val followingId = 2L
        val followingNickname = "following-456"

        val command = FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) }
        )
    }

    @Test
    fun `construct - success - accepts valid nicknames with Korean characters`() {
        val followerId = 1L
        val followerNickname = "팔로워"
        val followingId = 2L
        val followingNickname = "팔로잉대상"

        val command = FollowCreateCommand(followerId, followerNickname, followingId, followingNickname)

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) }
        )
    }
}
