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
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        val relationship = FriendRelationship(
            memberId,
            fromMemberNickname,
            memberProfileImageId,
            friendMemberId,
            friendNickname,
            friendProfileImageId
        )

        assertAll(
            { assertEquals(memberId, relationship.memberId) },
            { assertEquals(friendMemberId, relationship.friendMemberId) },
            { assertEquals(friendNickname, relationship.friendNickname) }
        )
    }

    @Test
    fun `create - failure - throws exception when memberId is zero`() {
        val memberId = 0L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val friendMemberId = 2L
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId is negative`() {
        val memberId = -1L
        val friendMemberId = 2L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendMemberId is zero`() {
        val memberId = 1L
        val friendMemberId = 0L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendMemberId is negative`() {
        val memberId = 1L
        val friendMemberId = -1L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId and friendMemberId are same`() {
        val memberId = 1L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                memberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_CANNOT_FRIEND_YOURSELF, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberProfileImageId is zero`() {
        val memberId = 1L
        val friendMemberId = 2L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 0L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberProfileImageId is negative`() {
        val memberId = 1L
        val friendMemberId = 2L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = -1L
        val friendProfileImageId = 4L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_MEMBER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendProfileImageId is zero`() {
        val memberId = 1L
        val friendMemberId = 2L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 0L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when friendProfileImageId is negative`() {
        val memberId = 1L
        val friendMemberId = 2L
        val fromMemberNickname = "sender"
        val friendNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = -1L

        assertFailsWith<IllegalArgumentException> {
            FriendRelationship(
                memberId,
                fromMemberNickname,
                memberProfileImageId,
                friendMemberId,
                friendNickname,
                friendProfileImageId
            )
        }.let {
            assertEquals(FriendRelationship.ERROR_FRIEND_PROFILE_IMAGE_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `of - success - creates valid relationship when command is valid`() {
        val fromMemberId = 50L
        val toMemberId = 75L
        val fromMemberNickname = "sender"
        val toMemberNickname = "receiver"
        val memberProfileImageId = 3L
        val friendProfileImageId = 4L
        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            memberProfileImageId,
            toMemberId,
            toMemberNickname,
            friendProfileImageId
        )

        val relationship = FriendRelationship.of(command)

        assertAll(
            { assertEquals(fromMemberId, relationship.memberId) },
            { assertEquals(toMemberId, relationship.friendMemberId) },
            { assertEquals(toMemberNickname, relationship.friendNickname) },
            { assertEquals(fromMemberNickname, relationship.memberNickname) }
        )
    }
}
