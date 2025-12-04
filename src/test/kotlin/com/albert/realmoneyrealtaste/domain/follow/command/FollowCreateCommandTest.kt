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
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        val command = FollowCreateCommand(
            followerId,
            followerNickname,
            followerProfileImageId,
            followingId,
            followingNickname,
            followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followerProfileImageId, command.followerProfileImageId) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) },
            { assertEquals(followingProfileImageId, command.followingProfileImageId) }
        )
    }

    @Test
    fun `construct - failure - throws exception when followerId is zero`() {
        val followerId = 0L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 0L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when following self`() {
        val memberId = 1L
        val nickname = "test"
        val profileImageId = 1L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                memberId,
                nickname,
                profileImageId,
                memberId,
                nickname,
                profileImageId
            )
        }.let {
            assertEquals("자기 자신을 팔로우할 수 없습니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerNickname is blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "   "
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingNickname is blank`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = ""
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "   "
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both nicknames are blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = ""
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            // followerNickname 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_NICKNAME_BLANK, it.message)
        }
    }

    @Test
    fun `construct - success - accepts valid nicknames with special characters`() {
        val followerId = 1L
        val followerNickname = "follower_123"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following-456"
        val followingProfileImageId = 2L

        val command = FollowCreateCommand(
            followerId,
            followerNickname,
            followerProfileImageId,
            followingId,
            followingNickname,
            followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followerProfileImageId, command.followerProfileImageId) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) },
            { assertEquals(followingProfileImageId, command.followingProfileImageId) }
        )
    }

    @Test
    fun `construct - success - accepts valid nicknames with Korean characters`() {
        val followerId = 1L
        val followerNickname = "팔로워"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "팔로잉대상"
        val followingProfileImageId = 2L

        val command = FollowCreateCommand(
            followerId,
            followerNickname,
            followerProfileImageId,
            followingId,
            followingNickname,
            followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followerNickname, command.followerNickname) },
            { assertEquals(followerProfileImageId, command.followerProfileImageId) },
            { assertEquals(followingId, command.followingId) },
            { assertEquals(followingNickname, command.followingNickname) },
            { assertEquals(followingProfileImageId, command.followingProfileImageId) }
        )
    }

    @Test
    fun `construct - failure - throws exception when followerProfileImageId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 0L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerProfileImageId is negative`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = -1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingProfileImageId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 0L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingProfileImageId is negative`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = -1L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(
                followerId,
                followerNickname,
                followerProfileImageId,
                followingId,
                followingNickname,
                followingProfileImageId
            )
        }.let {
            assertEquals(FollowCreateCommand.ERROR_FOLLOWING_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }
}
