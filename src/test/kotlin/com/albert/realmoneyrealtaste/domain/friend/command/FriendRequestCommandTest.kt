package com.albert.realmoneyrealtaste.domain.friend.command

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FriendRequestCommandTest {

    @Test
    fun `create - success - creates command with valid member IDs`() {
        val fromMemberId = 1L
        val toMemberId = 2L

        val command = FriendRequestCommand(fromMemberId, toMemberId)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) }
        )
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is zero`() {
        val fromMemberId = 0L
        val toMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is negative`() {
        val fromMemberId = -1L
        val toMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is zero`() {
        val fromMemberId = 1L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is negative`() {
        val fromMemberId = 1L
        val toMemberId = -1L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId and toMemberId are same`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(memberId, memberId)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are zero`() {
        val fromMemberId = 0L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            // fromMemberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are negative`() {
        val fromMemberId = -1L
        val toMemberId = -2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId)
        }.let {
            // fromMemberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - success - accepts large positive member IDs`() {
        val fromMemberId = Long.MAX_VALUE - 1
        val toMemberId = Long.MAX_VALUE

        val command = FriendRequestCommand(fromMemberId, toMemberId)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) }
        )
    }

    @Test
    fun `create - success - accepts minimum positive member IDs`() {
        val fromMemberId = 1L
        val toMemberId = 1L + 1

        val command = FriendRequestCommand(fromMemberId, toMemberId)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) }
        )
    }
}
