package com.albert.realmoneyrealtaste.domain.follow.command

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FollowCreateCommandTest {

    @Test
    fun `construct - success - creates command with valid ids`() {
        val followerId = 1L
        val followingId = 2L

        val command = FollowCreateCommand(followerId, followingId)

        assertAll(
            { assertEquals(followerId, command.followerId) },
            { assertEquals(followingId, command.followingId) }
        )
    }

    @Test
    fun `construct - failure - throws exception when followerId is zero`() {
        val followerId = 0L
        val followingId = 2L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followingId)
        }.let {
            assertEquals("팔로워 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when followingId is zero`() {
        val followerId = 1L
        val followingId = 0L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(followerId, followingId)
        }.let {
            assertEquals("팔로잉 대상 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when following self`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            FollowCreateCommand(memberId, memberId)
        }.let {
            assertEquals("자기 자신을 팔로우할 수 없습니다", it.message)
        }
    }
}
