package com.albert.realmoneyrealtaste.domain.friend.value

import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FriendRelationshipTest {

    @Test
    fun `create - success - creates relationship with valid member IDs`() {
        val memberId = 1L
        val friendMemberId = 2L

        val relationship = FriendRelationship(memberId, friendMemberId)

        assertAll(
            { assertEquals(memberId, relationship.memberId) },
            { assertEquals(friendMemberId, relationship.friendMemberId) }
        )
    }

    @Test
    fun `create - failure - throws exception when memberId is zero`() {
        val memberId = 0L
        val friendMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId is negative`() {
        val memberId = -1L
        val friendMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendMemberId is zero`() {
        val memberId = 1L
        val friendMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendMemberId is negative`() {
        val memberId = 1L
        val friendMemberId = -1L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId and friendMemberId are same`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, memberId)
        }.let {
            assertEquals(FriendRelationship.ERROR_CANNOT_FRIEND_YOURSELF, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are zero`() {
        val memberId = 0L
        val friendMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            // memberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both member IDs are negative`() {
        val memberId = -1L
        val friendMemberId = -2L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(memberId, friendMemberId)
        }.let {
            // memberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - success - accepts large positive member IDs`() {
        val memberId = Long.MAX_VALUE - 1
        val friendMemberId = Long.MAX_VALUE

        val relationship = FriendRelationship(memberId, friendMemberId)

        assertAll(
            { assertEquals(memberId, relationship.memberId) },
            { assertEquals(friendMemberId, relationship.friendMemberId) }
        )
    }

    @Test
    fun `create - success - accepts minimum positive member IDs`() {
        val memberId = 1L
        val friendMemberId = 2L

        val relationship = FriendRelationship(memberId, friendMemberId)

        assertAll(
            { assertEquals(memberId, relationship.memberId) },
            { assertEquals(friendMemberId, relationship.friendMemberId) }
        )
    }

    @Test
    fun `of - success - creates relationship from FriendRequestCommand`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val command = FriendRequestCommand(fromMemberId, toMemberId)

        val relationship = FriendRelationship.of(command)

        assertAll(
            { assertEquals(fromMemberId, relationship.memberId) },
            { assertEquals(toMemberId, relationship.friendMemberId) }
        )
    }

    @Test
    fun `of - success - maps command member IDs correctly`() {
        val fromMemberId = 100L
        val toMemberId = 200L
        val command = FriendRequestCommand(fromMemberId, toMemberId)

        val relationship = FriendRelationship.of(command)

        assertAll(
            { assertEquals(command.fromMemberId, relationship.memberId) },
            { assertEquals(command.toMemberId, relationship.friendMemberId) }
        )
    }

    @Test
    fun `of - success - creates valid relationship when command is valid`() {
        val fromMemberId = 50L
        val toMemberId = 75L
        val command = FriendRequestCommand(fromMemberId, toMemberId)

        val relationship = FriendRelationship.of(command)

        // 생성된 관계가 유효한지 확인 (예외가 발생하지 않음)
        assertAll(
            { assertEquals(fromMemberId, relationship.memberId) },
            { assertEquals(toMemberId, relationship.friendMemberId) }
        )
    }
}
