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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId
        )

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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberId and toMemberId are same`() {
        val memberId = 1L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                memberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                memberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(toMemberNickname, command.toMemberNickname) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(fromMemberNickname, command.fromMemberNickname) },
            { assertEquals(fromMemberId, command.fromMemberId) }
        )
    }

    @Test
    fun `create - failure - throws exception when toMemberNickname is blank`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "sender"
        val toMemberNickname = "   "
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
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
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                whitespaceNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when from nickname is empty`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val emptyNickname = ""
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                emptyNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when from nickname is blank`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "   "
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when from nickname is only whitespace`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberNickname = "\t\n\r "
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberProfileImageId is zero`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 0L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when fromMemberProfileImageId is negative`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = -1L
        val toMemberProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberProfileImageId is zero`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when toMemberProfileImageId is negative`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = -1L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_TO_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both profile image IDs are zero`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 0L
        val toMemberProfileImageId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both profile image IDs are negative`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = -1L
        val toMemberProfileImageId = -2L

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                fromMemberId,
                fromMemberNickname,
                fromMemberProfileImageId,
                toMemberId,
                toMemberNickname,
                toMemberProfileImageId,
            )
        }.let {
            assertEquals(FriendRequestCommand.ERROR_FROM_MEMBER_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - success - accepts large positive profile image IDs`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = Long.MAX_VALUE - 1
        val toMemberProfileImageId = Long.MAX_VALUE

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(fromMemberProfileImageId, command.fromMemberProfileImageId) },
            { assertEquals(toMemberProfileImageId, command.toMemberProfileImageId) }
        )
    }

    @Test
    fun `create - success - accepts minimum positive profile image IDs`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender"
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val fromMemberProfileImageId = 1L
        val toMemberProfileImageId = 1L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(fromMemberProfileImageId, command.fromMemberProfileImageId) },
            { assertEquals(toMemberProfileImageId, command.toMemberProfileImageId) }
        )
    }

    @Test
    fun `create - success - accepts valid nicknames with special characters`() {
        val fromMemberId = 1L
        val fromMemberNickname = "sender_123"
        val toMemberId = 2L
        val toMemberNickname = "receiver-abc"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(fromMemberNickname, command.fromMemberNickname) },
            { assertEquals(toMemberNickname, command.toMemberNickname) }
        )
    }

    @Test
    fun `create - success - accepts valid nicknames with Korean characters`() {
        val fromMemberId = 1L
        val fromMemberNickname = "홍길동"
        val toMemberId = 2L
        val toMemberNickname = "김철수"
        val fromMemberProfileImageId = 3L
        val toMemberProfileImageId = 4L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(fromMemberNickname, command.fromMemberNickname) },
            { assertEquals(toMemberNickname, command.toMemberNickname) }
        )
    }

    @Test
    fun `constants - success - has correct error messages`() {
        assertAll(
            { assertEquals("요청자 회원 ID는 양수여야 합니다", FriendRequestCommand.ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE) },
            { assertEquals("대상 회원 ID는 양수여야 합니다", FriendRequestCommand.ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE) },
            {
                assertEquals(
                    "자기 자신에게는 친구 요청을 보낼 수 없습니다",
                    FriendRequestCommand.ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF
                )
            },
            { assertEquals("대상 회원 닉네임은 비어있을 수 없습니다", FriendRequestCommand.ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY) },
            {
                assertEquals(
                    "요청자 회원 닉네임은 비어있을 수 없습니다",
                    FriendRequestCommand.ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY
                )
            },
            {
                assertEquals(
                    "요청자 회원 이미지 ID는 양수여야 합니다",
                    FriendRequestCommand.ERROR_FROM_MEMBER_IMAGE_ID_MUST_BE_POSITIVE
                )
            },
            { assertEquals("대상 회원 이미지 ID는 양수여야 합니다", FriendRequestCommand.ERROR_TO_MEMBER_IMAGE_ID_MUST_BE_POSITIVE) }
        )
    }

    @Test
    fun `create - success - verifies all properties are set correctly`() {
        val fromMemberId = 10L
        val fromMemberNickname = "요청자"
        val fromMemberProfileImageId = 100L
        val toMemberId = 20L
        val toMemberNickname = "수신자"
        val toMemberProfileImageId = 200L

        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )

        assertAll(
            { assertEquals(fromMemberId, command.fromMemberId) },
            { assertEquals(fromMemberNickname, command.fromMemberNickname) },
            { assertEquals(fromMemberProfileImageId, command.fromMemberProfileImageId) },
            { assertEquals(toMemberId, command.toMemberId) },
            { assertEquals(toMemberNickname, command.toMemberNickname) },
            { assertEquals(toMemberProfileImageId, command.toMemberProfileImageId) }
        )
    }
}
