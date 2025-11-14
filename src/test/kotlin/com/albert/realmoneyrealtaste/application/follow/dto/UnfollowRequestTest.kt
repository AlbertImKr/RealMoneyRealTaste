package com.albert.realmoneyrealtaste.application.follow.dto

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnfollowRequestTest {

    @Test
    fun `construct - success - creates UnfollowRequest with valid follower and following IDs`() {
        val followerId = 1L
        val followingId = 2L

        val request = UnfollowRequest(followerId, followingId)

        assertAll(
            { assertEquals(followerId, request.followerId) },
            { assertEquals(followingId, request.followingId) }
        )
    }

    @Test
    fun `construct - failure - throws exception when followerId is zero`() {
        val followerId = 0L
        val followingId = 2L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWER_ID, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerId is negative`() {
        val followerId = -1L
        val followingId = 2L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWER_ID, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingId is zero`() {
        val followerId = 1L
        val followingId = 0L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWING_ID, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingId is negative`() {
        val followerId = 1L
        val followingId = -1L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWING_ID, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followerId and followingId are same`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(memberId, memberId)
        }.let {
            assertEquals(UnfollowRequest.ERROR_MESSAGE_SELF_UNFOLLOW, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both IDs are zero`() {
        val followerId = 0L
        val followingId = 0L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            // followerId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWER_ID, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both IDs are negative`() {
        val followerId = -1L
        val followingId = -2L

        assertFailsWith<IllegalArgumentException> {
            UnfollowRequest(followerId, followingId)
        }.let {
            // followerId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(UnfollowRequest.ERROR_MESSAGE_INVALID_FOLLOWER_ID, it.message)
        }
    }

    @Test
    fun `construct - success - accepts large positive member IDs`() {
        val followerId = Long.MAX_VALUE - 1
        val followingId = Long.MAX_VALUE

        val request = UnfollowRequest(followerId, followingId)

        assertAll(
            { assertEquals(followerId, request.followerId) },
            { assertEquals(followingId, request.followingId) }
        )
    }

    @Test
    fun `construct - success - accepts minimum positive member IDs`() {
        val followerId = 1L
        val followingId = 2L

        val request = UnfollowRequest(followerId, followingId)

        assertAll(
            { assertEquals(followerId, request.followerId) },
            { assertEquals(followingId, request.followingId) }
        )
    }

    @Test
    fun `construct - success - handles reverse member IDs correctly`() {
        val followerId = 100L
        val followingId = 50L

        val request = UnfollowRequest(followerId, followingId)

        assertAll(
            { assertEquals(followerId, request.followerId) },
            { assertEquals(followingId, request.followingId) }
        )
    }
}
