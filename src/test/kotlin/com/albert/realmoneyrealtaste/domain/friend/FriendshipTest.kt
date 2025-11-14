package com.albert.realmoneyrealtaste.domain.friend

import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FriendshipTest {

    @Test
    fun `request - success - creates pending friendship with valid command`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val command = FriendRequestCommand(fromMemberId, toMemberId)
        val before = LocalDateTime.now()

        val friendship = Friendship.request(command)

        assertAll(
            { assertEquals(fromMemberId, friendship.relationShip.memberId) },
            { assertEquals(toMemberId, friendship.relationShip.friendMemberId) },
            { assertEquals(FriendshipStatus.PENDING, friendship.status) },
            { assertTrue(friendship.createdAt >= before) },
            { assertTrue(friendship.updatedAt >= before) },
            { assertEquals(friendship.createdAt, friendship.updatedAt) }
        )
    }

    @Test
    fun `request - failure - throws exception when requesting to self`() {
        val memberId = 1L
        val command = mockk<FriendRequestCommand> {
            every { fromMemberId } returns memberId
            every { toMemberId } returns memberId
        }

        assertFailsWith<IllegalArgumentException> {
            Friendship.request(command)
        }.let {
            assertEquals(Friendship.ERROR_CANNOT_SEND_REQUEST_TO_SELF, it.message)
        }
    }

    @Test
    fun `accept - success - changes status to accepted when pending`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        friendship.accept()

        assertAll(
            { assertEquals(FriendshipStatus.ACCEPTED, friendship.status) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `accept - failure - throws exception when status is not pending`() {
        val friendship = createPendingFriendship()
        friendship.accept() // 이미 수락된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            friendship.accept()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED, it.message)
        }
    }

    @Test
    fun `accept - failure - throws exception when status is rejected`() {
        val friendship = createPendingFriendship()
        friendship.reject() // 거절된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            friendship.accept()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED, it.message)
        }
    }

    @Test
    fun `reject - success - changes status to rejected when pending`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        friendship.reject()

        assertAll(
            { assertEquals(FriendshipStatus.REJECTED, friendship.status) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `reject - failure - throws exception when status is not pending`() {
        val friendship = createPendingFriendship()
        friendship.accept() // 수락된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            friendship.reject()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_REJECTED, it.message)
        }
    }

    @Test
    fun `reject - failure - throws exception when status is already rejected`() {
        val friendship = createPendingFriendship()
        friendship.reject() // 이미 거절된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            friendship.reject()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_REJECTED, it.message)
        }
    }

    @Test
    fun `unfriend - success - changes status to unfriended when accepted`() {
        val friendship = createAcceptedFriendship()
        val beforeUpdate = friendship.updatedAt

        friendship.unfriend()

        assertAll(
            { assertEquals(FriendshipStatus.UNFRIENDED, friendship.status) },
            { assertTrue(friendship.updatedAt >= beforeUpdate) }
        )
    }

    @Test
    fun `unfriend - failure - throws exception when status is pending`() {
        val friendship = createPendingFriendship()

        assertFailsWith<IllegalArgumentException> {
            friendship.unfriend()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_FRIENDS_CAN_UNFRIEND, it.message)
        }
    }

    @Test
    fun `unfriend - failure - throws exception when status is rejected`() {
        val friendship = createPendingFriendship()
        friendship.reject()

        assertFailsWith<IllegalArgumentException> {
            friendship.unfriend()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_FRIENDS_CAN_UNFRIEND, it.message)
        }
    }

    @Test
    fun `unfriend - failure - throws exception when status is already unfriended`() {
        val friendship = createAcceptedFriendship()
        friendship.unfriend() // 이미 해제된 상태로 변경

        assertFailsWith<IllegalArgumentException> {
            friendship.unfriend()
        }.let {
            assertEquals(Friendship.ERROR_ONLY_FRIENDS_CAN_UNFRIEND, it.message)
        }
    }

    @Test
    fun `isReceivedBy - success - returns true when member is the receiver`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isReceivedBy(toMemberId)

        assertTrue(result)
    }

    @Test
    fun `isReceivedBy - success - returns false when member is the sender`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isReceivedBy(fromMemberId)

        assertFalse(result)
    }

    @Test
    fun `isReceivedBy - success - returns false when member is not related`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val unrelatedMemberId = 3L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isReceivedBy(unrelatedMemberId)

        assertFalse(result)
    }

    @Test
    fun `isSentBy - success - returns true when member is the sender`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isSentBy(fromMemberId)

        assertTrue(result)
    }

    @Test
    fun `isSentBy - success - returns false when member is the receiver`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isSentBy(toMemberId)

        assertFalse(result)
    }

    @Test
    fun `isSentBy - success - returns false when member is not related`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val unrelatedMemberId = 3L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isSentBy(unrelatedMemberId)

        assertFalse(result)
    }

    @Test
    fun `isRelatedTo - success - returns true when member is the sender`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isRelatedTo(fromMemberId)

        assertTrue(result)
    }

    @Test
    fun `isRelatedTo - success - returns true when member is the receiver`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isRelatedTo(toMemberId)

        assertTrue(result)
    }

    @Test
    fun `isRelatedTo - success - returns false when member is not related`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val unrelatedMemberId = 3L
        val friendship = createFriendship(fromMemberId, toMemberId)

        val result = friendship.isRelatedTo(unrelatedMemberId)

        assertFalse(result)
    }

    @Test
    fun `constants - success - has correct error messages`() {
        assertAll(
            { assertEquals("대기 중인 친구 요청만 수락할 수 있습니다", Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED) },
            { assertEquals("대기 중인 친구 요청만 거절할 수 있습니다", Friendship.ERROR_ONLY_PENDING_REQUESTS_CAN_BE_REJECTED) },
            { assertEquals("친구 관계인 경우만 해제할 수 있습니다", Friendship.ERROR_ONLY_FRIENDS_CAN_UNFRIEND) },
            { assertEquals("자기 자신에게는 친구 요청을 보낼 수 없습니다", Friendship.ERROR_CANNOT_SEND_REQUEST_TO_SELF) }
        )
    }

    @Test
    fun `friendship lifecycle - success - complete workflow from request to unfriend`() {
        val command = FriendRequestCommand(1L, 2L)

        // 1. 친구 요청 생성
        val friendship = Friendship.request(command)
        assertEquals(FriendshipStatus.PENDING, friendship.status)

        // 2. 친구 요청 수락
        friendship.accept()
        assertEquals(FriendshipStatus.ACCEPTED, friendship.status)

        // 3. 친구 관계 해제
        friendship.unfriend()
        assertEquals(FriendshipStatus.UNFRIENDED, friendship.status)
    }

    @Test
    fun `friendship lifecycle - success - complete workflow from request to reject`() {
        val command = FriendRequestCommand(1L, 2L)

        // 1. 친구 요청 생성
        val friendship = Friendship.request(command)
        assertEquals(FriendshipStatus.PENDING, friendship.status)

        // 2. 친구 요청 거절
        friendship.reject()
        assertEquals(FriendshipStatus.REJECTED, friendship.status)
    }

    private fun createPendingFriendship(): Friendship {
        val command = FriendRequestCommand(1L, 2L)
        return Friendship.request(command)
    }

    private fun createAcceptedFriendship(): Friendship {
        val friendship = createPendingFriendship()
        friendship.accept()
        return friendship
    }

    private fun createFriendship(fromMemberId: Long, toMemberId: Long): Friendship {
        val command = FriendRequestCommand(fromMemberId, toMemberId)
        return Friendship.request(command)
    }
}
