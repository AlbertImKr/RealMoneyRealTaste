package com.albert.realmoneyrealtaste.application.follow.dto

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.FollowStatus
import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals

class FollowResponseTest {

    @Test
    fun `from - success - creates response with all follow information`() {
        val followerId = 1L
        val followerNickname = "follower"
        val followerProfileImageId = 1L
        val followingId = 2L
        val followingNickname = "following"
        val followingProfileImageId = 2L
        val command = FollowCreateCommand(
            followerId = followerId,
            followerNickname = followerNickname,
            followerProfileImageId = followerProfileImageId,
            followingId = followingId,
            followingNickname = followingNickname,
            followingProfileImageId = followingProfileImageId,
        )
        val follow = Follow.create(command)

        // ID 설정 (실제로는 JPA가 수행)
        setFollowId(follow, 100L)

        val result = FollowResponse.from(follow)

        assertAll(
            { assertEquals(100L, result.followId) },
            { assertEquals(followerId, result.followerId) },
            { assertEquals(followerNickname, result.followerNickname) },
            { assertEquals(followingId, result.followingId) },
            { assertEquals(followingNickname, result.followingNickname) },
            { assertEquals(FollowStatus.ACTIVE, result.status) },
            { assertEquals(follow.createdAt, result.createdAt) },
            { assertEquals(follow.updatedAt, result.updatedAt) },
            { assertEquals(followerProfileImageId, result.followerProfileImageId) },
            { assertEquals(followingProfileImageId, result.followingProfileImageId) },
        )
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
