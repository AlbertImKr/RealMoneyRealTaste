package com.albert.realmoneyrealtaste.domain.friend

import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.friend.value.FriendRelationship
import com.albert.realmoneyrealtaste.util.setId
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
        val fromMemberNickname = "sender"
        val fromMemberProfileImageId = 1L
        val toMemberId = 2L
        val toMemberNickname = "receiver"
        val toMemberProfileImageId = 2L
        val command = FriendRequestCommand(
            fromMemberId,
            fromMemberNickname,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId,
        )
        val before = LocalDateTime.now()

        val friendship = Friendship.request(command)

        assertAll(
            { assertEquals(fromMemberId, friendship.relationShip.memberId) },
            { assertEquals(toMemberId, friendship.relationShip.friendMemberId) },
            { assertEquals(toMemberNickname, friendship.relationShip.friendNickname) },
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
            every { toMemberNickname } returns "self"
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
        val command = createFriendRequestCommand(1L, "sender")

        // 1. 친구 요청 생성
        val friendship = Friendship.request(command).also { it.setId() }
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
        val command = createFriendRequestCommand()

        // 1. 친구 요청 생성
        val friendship = Friendship.request(command).also { it.setId() }
        assertEquals(FriendshipStatus.PENDING, friendship.status)

        // 2. 친구 요청 거절
        friendship.reject()
        assertEquals(FriendshipStatus.REJECTED, friendship.status)
    }

    @Test
    fun `rePending - success - changes status to pending when unfriended`() {
        val friendship = createAcceptedFriendship()
        friendship.unfriend() // UNFRIENDED 상태로 변경
        val beforeUpdate = friendship.updatedAt

        friendship.rePending()

        assertAll(
            { assertEquals(FriendshipStatus.PENDING, friendship.status) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `rePending - success - changes status to pending when rejected`() {
        val friendship = createPendingFriendship()
        friendship.reject() // REJECTED 상태로 변경
        val beforeUpdate = friendship.updatedAt

        friendship.rePending()

        assertAll(
            { assertEquals(FriendshipStatus.PENDING, friendship.status) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `rePending - failure - throws exception when status is pending`() {
        val friendship = createPendingFriendship()

        assertFailsWith<IllegalArgumentException> {
            friendship.rePending()
        }.let {
            assertEquals("친구 요청을 다시 보낼 수 없는 상태입니다. 현재 상태: PENDING", it.message)
        }
    }

    @Test
    fun `rePending - failure - throws exception when status is accepted`() {
        val friendship = createAcceptedFriendship()

        assertFailsWith<IllegalArgumentException> {
            friendship.rePending()
        }.let {
            assertEquals("친구 요청을 다시 보낼 수 없는 상태입니다. 현재 상태: ACCEPTED", it.message)
        }
    }

    @Test
    fun `updateMemberInfo - success - updates sender nickname and image ID`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt
        val newNickname = "새로운닉네임"
        val newImageId = 999L

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = newNickname,
            imageId = newImageId
        )

        assertAll(
            { assertEquals(newNickname, friendship.relationShip.memberNickname) },
            { assertEquals(newImageId, friendship.relationShip.memberProfileImageId) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates friend nickname and image ID`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt
        val newNickname = "친구새닉네임"
        val newImageId = 888L

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = newNickname,
            imageId = newImageId
        )

        assertAll(
            { assertEquals(newNickname, friendship.relationShip.friendNickname) },
            { assertEquals(newImageId, friendship.relationShip.friendProfileImageId) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates only nickname when image ID is null`() {
        val friendship = createPendingFriendship()
        val originalImageId = friendship.relationShip.memberProfileImageId
        val newNickname = "닉네임만변경"

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = newNickname,
            imageId = null
        )

        assertAll(
            { assertEquals(newNickname, friendship.relationShip.memberNickname) },
            { assertEquals(originalImageId, friendship.relationShip.memberProfileImageId) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates only image ID when nickname is null`() {
        val friendship = createPendingFriendship()
        val originalNickname = friendship.relationShip.memberNickname
        val newImageId = 777L

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = null,
            imageId = newImageId
        )

        assertAll(
            { assertEquals(originalNickname, friendship.relationShip.memberNickname) },
            { assertEquals(newImageId, friendship.relationShip.memberProfileImageId) }
        )
    }

    @Test
    fun `updateMemberInfo - success - does nothing when member is not related`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt
        val originalNickname = friendship.relationShip.memberNickname
        val originalImageId = friendship.relationShip.memberProfileImageId

        friendship.updateMemberInfo(
            memberId = 999L, // 관련 없는 회원 ID
            nickname = "변경안됨",
            imageId = 555L
        )

        assertAll(
            { assertEquals(originalNickname, friendship.relationShip.memberNickname) },
            { assertEquals(originalImageId, friendship.relationShip.memberProfileImageId) },
            { assertTrue(beforeUpdate.isEqual(friendship.updatedAt)) } // updatedAt 변경 없음
        )
    }

    @Test
    fun `updateMemberInfo - success - updates only friend nickname when image ID is null`() {
        val friendship = createPendingFriendship()
        val originalImageId = friendship.relationShip.friendProfileImageId
        val newNickname = "친구닉네임만변경"

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = newNickname,
            imageId = null
        )

        assertAll(
            { assertEquals(newNickname, friendship.relationShip.friendNickname) },
            { assertEquals(originalImageId, friendship.relationShip.friendProfileImageId) },
            { assertTrue(friendship.updatedAt > friendship.createdAt) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates only friend image ID when nickname is null`() {
        val friendship = createPendingFriendship()
        val originalNickname = friendship.relationShip.friendNickname
        val newImageId = 666L

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = null,
            imageId = newImageId
        )

        assertAll(
            { assertEquals(originalNickname, friendship.relationShip.friendNickname) },
            { assertEquals(newImageId, friendship.relationShip.friendProfileImageId) },
            { assertTrue(friendship.updatedAt > friendship.createdAt) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates friend info multiple times`() {
        val friendship = createPendingFriendship()

        // 첫 번째 업데이트
        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = "첫번째닉네임",
            imageId = 111L
        )

        val firstUpdate = friendship.updatedAt

        // 잠시 대기
        Thread.sleep(1)

        // 두 번째 업데이트
        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = "두번째닉네임",
            imageId = 222L
        )

        assertAll(
            { assertEquals("두번째닉네임", friendship.relationShip.friendNickname) },
            { assertEquals(222L, friendship.relationShip.friendProfileImageId) },
            { assertTrue(friendship.updatedAt > firstUpdate) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates friend info after unfriend`() {
        val friendship = createAcceptedFriendship()
        friendship.unfriend() // UNFRIENDED 상태로 변경

        val beforeUpdate = friendship.updatedAt

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = "해제후닉네임",
            imageId = 555L
        )

        assertAll(
            { assertEquals("해제후닉네임", friendship.relationShip.friendNickname) },
            { assertEquals(555L, friendship.relationShip.friendProfileImageId) },
            { assertTrue(friendship.updatedAt > beforeUpdate) }
        )
    }

    @Test
    fun `updateMemberInfo - success - updates friend info when sender info is unchanged`() {
        val friendship = createPendingFriendship()
        val originalSenderNickname = friendship.relationShip.memberNickname
        val originalSenderImageId = friendship.relationShip.memberProfileImageId

        val newFriendNickname = "친구만변경"
        val newFriendImageId = 999L

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = newFriendNickname,
            imageId = newFriendImageId
        )

        assertAll(
            { assertEquals(originalSenderNickname, friendship.relationShip.memberNickname) },
            { assertEquals(originalSenderImageId, friendship.relationShip.memberProfileImageId) },
            { assertEquals(newFriendNickname, friendship.relationShip.friendNickname) },
            { assertEquals(newFriendImageId, friendship.relationShip.friendProfileImageId) }
        )
    }

    @Test
    fun `updateMemberInfo - success - preserves friend info when updating sender`() {
        val friendship = createPendingFriendship()
        val originalFriendNickname = friendship.relationShip.friendNickname
        val originalFriendImageId = friendship.relationShip.friendProfileImageId

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = "보내는사람변경",
            imageId = 123L
        )

        assertAll(
            { assertEquals("보내는사람변경", friendship.relationShip.memberNickname) },
            { assertEquals(123L, friendship.relationShip.memberProfileImageId) },
            { assertEquals(originalFriendNickname, friendship.relationShip.friendNickname) },
            { assertEquals(originalFriendImageId, friendship.relationShip.friendProfileImageId) }
        )
    }

    @Test
    fun `updateMemberInfo - success - does not update updatedAt when both parameters are null`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = null,
            imageId = null
        )

        assertTrue(beforeUpdate.isEqual(friendship.updatedAt)) // updatedAt 변경 없음
    }

    @Test
    fun `updateMemberInfo - success - does not update updatedAt when both parameters are null for friend`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.friendMemberId,
            nickname = null,
            imageId = null
        )

        assertTrue(beforeUpdate.isEqual(friendship.updatedAt)) // updatedAt 변경 없음
    }

    @Test
    fun `updateMemberInfo - success - updates updatedAt when only nickname is provided`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        Thread.sleep(1)

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = "새닉네임",
            imageId = null
        )

        assertTrue(friendship.updatedAt.isAfter(beforeUpdate))
    }

    @Test
    fun `updateMemberInfo - success - updates updatedAt when only imageId is provided`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        Thread.sleep(1)

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = null,
            imageId = 123L
        )

        assertTrue(friendship.updatedAt.isAfter(beforeUpdate))
    }

    @Test
    fun `updateMemberInfo - boundary - empty string nickname still updates updatedAt`() {
        val friendship = createPendingFriendship()
        val beforeUpdate = friendship.updatedAt

        Thread.sleep(1)

        friendship.updateMemberInfo(
            memberId = friendship.relationShip.memberId,
            nickname = "", // 빈 문자열
            imageId = null
        )

        assertTrue(friendship.updatedAt.isAfter(beforeUpdate))
        assertEquals("", friendship.relationShip.memberNickname)
    }

    @Test
    fun `drainDomainEvents - success - returns FriendRequestSentEvent with actual ID when friendship is created`() {
        val friendship = Friendship.request(createFriendRequestCommand())
        friendship.setId(123L)

        val events = friendship.drainDomainEvents()

        assertEquals(1, events.size)
        assertTrue(events[0] is FriendRequestSentEvent)

        val event = events[0] as FriendRequestSentEvent
        assertEquals(123L, event.friendshipId)
        assertEquals(1L, event.fromMemberId)
        assertEquals(2L, event.toMemberId)
    }

    @Test
    fun `drainDomainEvents - success - returns empty list when called twice`() {
        val friendship = Friendship.request(createFriendRequestCommand())
        friendship.setId(123L)

        // 첫 번째 호출
        val firstEvents = friendship.drainDomainEvents()
        assertEquals(1, firstEvents.size)

        // 두 번째 호출은 빈 리스트 반환
        val secondEvents = friendship.drainDomainEvents()
        assertEquals(0, secondEvents.size)
    }

    @Test
    fun `drainDomainEvents - success - returns FriendRequestAcceptedEvent with actual ID when accepted`() {
        val friendship = createPendingFriendship()
        friendship.setId(456L)
        friendship.accept()

        val events = friendship.drainDomainEvents()

        assertEquals(2, events.size) // 생성 + 수락 이벤트
        assertTrue(events[1] is FriendRequestAcceptedEvent)

        val acceptEvent = events[1] as FriendRequestAcceptedEvent
        assertEquals(456L, acceptEvent.friendshipId)
        assertEquals(1L, acceptEvent.fromMemberId)
        assertEquals(2L, acceptEvent.toMemberId)
    }

    @Test
    fun `drainDomainEvents - success - returns FriendRequestRejectedEvent with actual ID when rejected`() {
        val friendship = createPendingFriendship()
        friendship.setId(789L)
        friendship.reject()

        val events = friendship.drainDomainEvents()

        assertEquals(2, events.size) // 생성 + 거절 이벤트
        assertTrue(events[1] is FriendRequestRejectedEvent)

        val rejectEvent = events[1] as FriendRequestRejectedEvent
        assertEquals(789L, rejectEvent.friendshipId)
        assertEquals(1L, rejectEvent.fromMemberId)
        assertEquals(2L, rejectEvent.toMemberId)
    }

    @Test
    fun `drainDomainEvents - success - returns FriendshipTerminatedEvent with actual ID when unfriended`() {
        val friendship = createAcceptedFriendship()
        friendship.setId(999L)
        friendship.unfriend()

        val events = friendship.drainDomainEvents()

        assertEquals(3, events.size) // 생성 + 수락 + 해제 이벤트
        assertTrue(events[2] is FriendshipTerminatedEvent)

        val terminateEvent = events[2] as FriendshipTerminatedEvent
        assertEquals(999L, terminateEvent.friendshipId)
        assertEquals(1L, terminateEvent.memberId)
        assertEquals(2L, terminateEvent.friendMemberId)
    }

    @Test
    fun `drainDomainEvents - success - handles multiple events in correct order`() {
        val friendship = createPendingFriendship()
        friendship.setId(555L)

        // 수락 후 해제
        friendship.accept()
        friendship.unfriend()

        val events = friendship.drainDomainEvents()

        assertEquals(3, events.size)
        assertTrue(events[0] is FriendRequestSentEvent)
        assertTrue(events[1] is FriendRequestAcceptedEvent)
        assertTrue(events[2] is FriendshipTerminatedEvent)

        // 모든 이벤트의 friendshipId가 실제 ID로 설정되었는지 확인
        events.forEach { event ->
            when (event) {
                is FriendRequestSentEvent -> assertEquals(555L, event.friendshipId)
                is FriendRequestAcceptedEvent -> assertEquals(555L, event.friendshipId)
                is FriendshipTerminatedEvent -> assertEquals(555L, event.friendshipId)
            }
        }
    }

    @Test
    fun `drainDomainEvents - success - includes events from rePending`() {
        val friendship = createAcceptedFriendship()
        friendship.setId(666L)
        friendship.unfriend() // UNFRIENDED 상태
        friendship.rePending() // 다시 PENDING 상태

        val events = friendship.drainDomainEvents()

        assertEquals(4, events.size) // 생성 + 해제 + 재요청 이벤트
        assertTrue(events[3] is FriendRequestSentEvent)

        val rePendingEvent = events[3] as FriendRequestSentEvent
        assertEquals(666L, rePendingEvent.friendshipId)
        assertEquals(1L, rePendingEvent.fromMemberId)
        assertEquals(2L, rePendingEvent.toMemberId)
    }

    @Test
    fun `setter - success - for coverage`() {
        val friendship = TestFriendship(
            relationShip = FriendRelationship.of(
                FriendRequestCommand(
                    fromMemberId = 1L,
                    fromMemberNickname = "홍길동",
                    fromMemberProfileImageId = 3L,
                    toMemberId = 2L,
                    toMemberNickname = "김철수",
                    toMemberProfileImageId = 4L,
                )
            )
        )
        val newCreatedAt = LocalDateTime.now()

        friendship.setCreatedAtForTest(newCreatedAt)

        assertTrue { newCreatedAt.isEqual(friendship.createdAt) }
    }

    private class TestFriendship(
        relationShip: FriendRelationship,
        status: FriendshipStatus = FriendshipStatus.PENDING,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
    ) : Friendship(
        relationShip = relationShip,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    ) {
        fun setCreatedAtForTest(createdAt: LocalDateTime) {
            this.createdAt = createdAt
        }
    }

    private fun createPendingFriendship(): Friendship {
        val command = createFriendRequestCommand()
        return Friendship.request(command)
            .also { it.setId() }
    }

    private fun createAcceptedFriendship(): Friendship {
        val friendship = createPendingFriendship()
        friendship.accept()
        return friendship
    }

    private fun createFriendship(fromMemberId: Long, toMemberId: Long): Friendship {
        val command = createFriendRequestCommand(
            fromMemberId = fromMemberId,
            toMemberId = toMemberId
        )
        return Friendship.request(command)
            .also { it.setId() }
    }

    private fun createFriendRequestCommand(
        fromMemberId: Long = 1L,
        fromMemberNickName: String = "sender",
        fromMemberProfileImageId: Long = 3L,
        toMemberId: Long = 2L,
        toMemberNickname: String = "receiver",
        toMemberProfileImageId: Long = 4L,
    ): FriendRequestCommand {
        return FriendRequestCommand(
            fromMemberId,
            fromMemberNickName,
            fromMemberProfileImageId,
            toMemberId,
            toMemberNickname,
            toMemberProfileImageId
        )
    }
}
