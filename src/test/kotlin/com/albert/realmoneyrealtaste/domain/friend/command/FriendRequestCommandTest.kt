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
        val toMemberNickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, toMemberId, toMemberNickname)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(toMemberNickname, command.toMemberNickname) }
        )
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is zero`() {
        val fromMemberId = 0L
        val toMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId is negative`() {
        val fromMemberId = -1L
        val toMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is zero`() {
        val fromMemberId = 1L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberId is negative`() {
        val fromMemberId = 1L
        val toMemberId = -1L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId and toMemberId are same`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(memberId, memberId, "receiver")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are zero`() {
        val fromMemberId = 0L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
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
            FriendRequestCommand(fromMemberId, toMemberId, "receiver")
        }.let {
            // fromMemberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - success - accepts large positive member IDs`() {
        val fromMemberId = Long.MAX_VALUE - 1
        val toMemberId = Long.MAX_VALUE

        val command = FriendRequestCommand(fromMemberId, toMemberId, "receiver")

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

        val command = FriendRequestCommand(fromMemberId, toMemberId, "receiver")

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
        val emptyNickname = ""

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, emptyNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - success - accepts valid toMemberNickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val nickname = "testUser"

        val command = FriendRequestCommand(fromMemberId, toMemberId, nickname)

        assertEquals(nickname, command.toMemberNickname)
    }

    @Test
    fun `create - failure - throws exception when toMemberNickname is blank`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val blankNickname = "   "

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, blankNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - success - accepts special characters in nickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val specialNickname = "특수문자_한글123!@#"

        val command = FriendRequestCommand(fromMemberId, toMemberId, specialNickname)

        assertEquals(specialNickname, command.toMemberNickname)
    }

    @Test
    fun `create - success - accepts very long nickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val longNickname = "a".repeat(1000) // 매우 긴 닉네임

        val command = FriendRequestCommand(fromMemberId, toMemberId, longNickname)

        assertEquals(longNickname, command.toMemberNickname)
    }

    @Test
    fun `create - success - accepts unicode characters in nickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val unicodeNickname = "🎉测试用户😊"

        val command = FriendRequestCommand(fromMemberId, toMemberId, unicodeNickname)

        assertEquals(unicodeNickname, command.toMemberNickname)
    }

    @Test
    fun `create - success - accepts nickname with spaces`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val nicknameWithSpaces = "John Doe"

        val command = FriendRequestCommand(fromMemberId, toMemberId, nicknameWithSpaces)

        assertEquals(nicknameWithSpaces, command.toMemberNickname)
    }

    @Test
    fun `create - failure - throws exception when nickname is only whitespace`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val whitespaceNickname = "\t\n\r "

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, whitespaceNickname)
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - success - accepts single character nickname`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val singleCharNickname = "A"

        val command = FriendRequestCommand(fromMemberId, toMemberId, singleCharNickname)

        assertEquals(singleCharNickname, command.toMemberNickname)
    }

    @Test
    fun `create - success - maintains order of member IDs`() {
        val fromMemberId = 100L
        val toMemberId = 200L
        val nickname = "receiver"

        val command = FriendRequestCommand(fromMemberId, toMemberId, nickname)

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(nickname, command.toMemberNickname) }
        )
    }

    @Test
    fun `create - failure - validates fromMemberId before toMemberId`() {
        val fromMemberId = 0L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "test")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - validates toMemberId before nickname check`() {
        val fromMemberId = 1L
        val toMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(fromMemberId, toMemberId, "")
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }
}
