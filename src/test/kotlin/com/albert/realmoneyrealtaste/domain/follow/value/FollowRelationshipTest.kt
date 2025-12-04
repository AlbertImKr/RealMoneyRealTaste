package com.albert.realmoneyrealtaste.domain.follow.value

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FollowRelationshipTest {

    @Test
    fun `create - success - creates relationship with valid follower and following IDs`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, relationship.followerId) },
            { assertEquals(followerNickname, relationship.followerNickname) },
            { assertEquals(followerProfileImageId, relationship.followerProfileImageId) },
            { assertEquals(followingId, relationship.followingId) },
            { assertEquals(followingNickname, relationship.followingNickname) },
            { assertEquals(followingProfileImageId, relationship.followingProfileImageId) }
        )
    }

    @Test
    fun `create - failure - throws exception when followerId is zero`() {
        val followerId = 0L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followerId is negative`() {
        val followerId = -1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 0L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingId is negative`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = -1L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followerId and followingId are same`() {
        val memberId = 1L
        val nickname = "test"
        val profileImageId = 1L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = memberId,
                followerNickname = nickname,
                followerProfileImageId = profileImageId,
                followingId = memberId,
                followingNickname = nickname,
                followingProfileImageId = profileImageId
            )
        }.let {
            assertEquals("자기 자신을 팔로우할 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both IDs are zero`() {
        val followerId = 0L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 0L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            // followerId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both IDs are negative`() {
        val followerId = -1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = -2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            // followerId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followerNickname is blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 닉네임은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followerNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "   "
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 닉네임은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingNickname is blank`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = ""
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 닉네임은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingNickname is only whitespace`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "   "
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 닉네임은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both nicknames are blank`() {
        val followerId = 1L
        val followerNickname = ""
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = ""
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            // followerNickname 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals("팔로워 닉네임은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `create - success - accepts valid nicknames with special characters`() {
        val followerId = 1L
        val followerNickname = "follower_123"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following-456"
        val followingProfileImageId = 2L

        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, relationship.followerId) },
            { assertEquals(followerNickname, relationship.followerNickname) },
            { assertEquals(followerProfileImageId, relationship.followerProfileImageId) },
            { assertEquals(followingId, relationship.followingId) },
            { assertEquals(followingNickname, relationship.followingNickname) },
            { assertEquals(followingProfileImageId, relationship.followingProfileImageId) }
        )
    }

    @Test
    fun `create - success - accepts valid nicknames with Korean characters`() {
        val followerId = 1L
        val followerNickname = "팔로워"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "팔로잉대상"
        val followingProfileImageId = 2L

        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, relationship.followerId) },
            { assertEquals(followerNickname, relationship.followerNickname) },
            { assertEquals(followerProfileImageId, relationship.followerProfileImageId) },
            { assertEquals(followingId, relationship.followingId) },
            { assertEquals(followingNickname, relationship.followingNickname) },
            { assertEquals(followingProfileImageId, relationship.followingProfileImageId) }
        )
    }

    @Test
    fun `create - success - accepts large positive member IDs`() {
        val followerId = Long.MAX_VALUE - 1
        val followerNickname = "follower"
        val followerProfileImageId = Long.MAX_VALUE - 1
        val followingId = Long.MAX_VALUE
        val followingNickname = "following"
        val followingProfileImageId = Long.MAX_VALUE

        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, relationship.followerId) },
            { assertEquals(followerNickname, relationship.followerNickname) },
            { assertEquals(followerProfileImageId, relationship.followerProfileImageId) },
            { assertEquals(followingId, relationship.followingId) },
            { assertEquals(followingNickname, relationship.followingNickname) },
            { assertEquals(followingProfileImageId, relationship.followingProfileImageId) }
        )
    }

    @Test
    fun `create - success - accepts minimum positive member IDs`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        assertAll(
            { assertEquals(followerId, relationship.followerId) },
            { assertEquals(followerNickname, relationship.followerNickname) },
            { assertEquals(followerProfileImageId, relationship.followerProfileImageId) },
            { assertEquals(followingId, relationship.followingId) },
            { assertEquals(followingNickname, relationship.followingNickname) },
            { assertEquals(followingProfileImageId, relationship.followingProfileImageId) }
        )
    }

    @Test
    fun `isFollower - success - returns true when member is the follower`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollower(followerId)

        assertTrue(result)
    }

    @Test
    fun `isFollower - success - returns false when member is not the follower`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val otherId = 3L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollower(otherId)

        assertFalse(result)
    }

    @Test
    fun `isFollower - success - returns false when member is the following target`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollower(followingId)

        assertFalse(result)
    }

    @Test
    fun `isFollowing - success - returns true when member is the following target`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollowing(followingId)

        assertTrue(result)
    }

    @Test
    fun `isFollowing - success - returns false when member is not the following target`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val otherId = 3L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollowing(otherId)

        assertFalse(result)
    }

    @Test
    fun `isFollowing - success - returns false when member is the follower`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val relationship = FollowRelationship(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId
        )

        val result = relationship.isFollowing(followerId)

        assertFalse(result)
    }

    @Test
    fun `create - failure - throws exception when followerProfileImageId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 0L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 프로필 이미지 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followerProfileImageId is negative`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = -1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로워 프로필 이미지 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingProfileImageId is zero`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 0L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 프로필 이미지 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when followingProfileImageId is negative`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = -1L

        assertFailsWith<IllegalArgumentException> {
            FollowRelationship(
                followerId = followerId,
                followerNickname = followerNickname,
                followerProfileImageId = followerProfileImageId,
                followingId = followingId,
                followingNickname = followingNickname,
                followingProfileImageId = followingProfileImageId
            )
        }.let {
            assertEquals("팔로잉 대상 프로필 이미지 ID는 양수여야 합니다", it.message)
        }
    }
}
