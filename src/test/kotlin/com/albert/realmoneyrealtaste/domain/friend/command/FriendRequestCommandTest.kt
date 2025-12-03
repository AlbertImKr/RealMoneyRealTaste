package com.albert.realmoneyrealtaste.domain.friend.command

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FriendRequestCommandTest {

    @Test
    fun `create - success - creates command with valid member IDs`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(toMemberNickname, command.toMemberNickname) }
        )
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is zero`() {
        val fromMemberId = 0L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is negative`() {
        val fromMemberId = -1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is zero`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 0L
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is negative`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = -1L
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId and toMemberId are same`() {
        val memberId = 1L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(memberId, fromMemberNickname, memberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are zero`() {
        val fromMemberId = 0L
        val toMemberId = 0L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            // fromMemberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are negative`() {
        val fromMemberId = -1L
        val toMemberId = -2L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            // fromMemberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - success - accepts large positive member IDs`() {
        val fromMemberId = Long.MAX_VALUE - 1
        val toMemberId = Long.MAX_VALUE
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals("receiver", command.toMemberNickname) }
        )
    }

    @Test
    fun `create - success - accepts minimum positive member IDs`() {
        val fromMemberId = 1L
        val toMemberId = 1L + 1
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals("receiver", command.toMemberNickname) }
        )
    }

    @Test
    fun `create - failure - throws exception when toMemberNickname is empty`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "sender"
        val toMemberNickname = ""

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - success - accepts valid toMemberNickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)

        assertAll(
            { assertEquals(toMemberNickname, command.toMemberNickname) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(fromMemberNickname, command.fromMemberNickName) },
            { assertEquals(fromMemberId, command.fromMemberId) }
        )
    }

    @Test
    fun `create - failure - throws exception when toMemberNickname is blank`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "sender"
        val toMemberNickname = "   "

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when nickname is only whitespace`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "sender"
        val whitespaceNickname = "\t\n\r "

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, whitespaceNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }
}
