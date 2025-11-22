package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.exception.FriendResponseException
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RecordApplicationEvents
class FriendResponderTest(
    private val friendResponder: FriendResponder,
    private val friendRequestor: FriendRequestor,
    private val friendshipReader: FriendshipReader,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `respondToFriendRequest - success - accepts friend request and creates reverse friendship`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "accept-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "accept-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청 생성
        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)
        flushAndClear()
        applicationEvents.clear()

        // 친구 요청 수락
        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )
        val result = friendResponder.respondToFriendRequest(request)

        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(FriendshipStatus.ACCEPTED, result.status) },
            { assertNotNull(result.updatedAt) }
        )

        // 양방향 친구 관계 생성 확인
        val savedFriendship = friendshipReader.findActiveFriendship(
            sender.requireId(), receiver.requireId()
        )
        val reverseFriendship = friendshipReader.findActiveFriendship(
            receiver.requireId(), sender.requireId()
        )
        val allFriendships = listOfNotNull(savedFriendship, reverseFriendship)

        val acceptedFriendships = allFriendships.filter { it.status == FriendshipStatus.ACCEPTED }
        assertEquals(2, acceptedFriendships.size)

        // 이벤트 발행 확인
        val events = applicationEvents.stream(FriendRequestAcceptedEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(friendship.requireId(), event.friendshipId) },
            { assertEquals(sender.requireId(), event.fromMemberId) },
            { assertEquals(receiver.requireId(), event.toMemberId) }
        )
    }

    @Test
    fun `respondToFriendRequest - success - rejects friend request and publishes event`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "reject-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "reject-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청 생성
        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)
        flushAndClear()
        applicationEvents.clear()

        // 친구 요청 거절
        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = false
        )
        val result = friendResponder.respondToFriendRequest(request)

        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(FriendshipStatus.REJECTED, result.status) },
            { assertNotNull(result.updatedAt) }
        )

        // 양방향 친구 관계가 생성되지 않음을 확인
        val reverseFriendship = friendshipReader.findActiveFriendship(
            sender.requireId(), receiver.requireId()
        )
        val allFriendships = listOfNotNull(friendship, reverseFriendship)
        val acceptedFriendships = allFriendships.filter { it.status == FriendshipStatus.ACCEPTED }
        assertEquals(0, acceptedFriendships.size)

        // 이벤트 발행 확인
        val events = applicationEvents.stream(FriendRequestRejectedEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(friendship.requireId(), event.friendshipId) },
            { assertEquals(sender.requireId(), event.fromMemberId) },
            { assertEquals(receiver.requireId(), event.toMemberId) }
        )
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when respondent does not exist`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "auth-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "auth-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        val nonExistentMemberId = 999999L
        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = nonExistentMemberId,
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(request)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when friendship does not exist`() {
        val receiver = testMemberHelper.createActivatedMember(
            email = "notfound-receiver@test.com",
            nickname = "receiver"
        )

        val nonExistentFriendshipId = 999999L
        val request = FriendResponseRequest(
            friendshipId = nonExistentFriendshipId,
            respondentMemberId = receiver.requireId(),
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(request)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when respondent is not the receiver`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "unauth-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "unauth-receiver@test.com",
            nickname = "receiver"
        )
        val unauthorized = testMemberHelper.createActivatedMember(
            email = "unauthorized@test.com",
            nickname = "unauthorized"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        // 요청을 받지 않은 사람이 응답 시도
        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = unauthorized.requireId(), // 권한 없는 사용자
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(request)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when sender tries to respond to own request`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "self-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "self-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        // 요청을 보낸 사람이 응답 시도
        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = sender.requireId(), // 요청 보낸 사람
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(request)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when friendship is already accepted`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "already-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "already-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        // 첫 번째 응답 (수락)
        val acceptRequest = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )
        friendResponder.respondToFriendRequest(acceptRequest)

        // 두 번째 응답 시도 (이미 수락된 상태)
        val duplicateRequest = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = false
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(duplicateRequest)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when friendship is already rejected`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "rejected-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "rejected-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        // 첫 번째 응답 (거절)
        val rejectRequest = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = false
        )
        friendResponder.respondToFriendRequest(rejectRequest)

        // 두 번째 응답 시도 (이미 거절된 상태)
        val duplicateRequest = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(duplicateRequest)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `respondToFriendRequest - success - updates friendship status and timestamp correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "timestamp-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "timestamp-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )

        val result = friendResponder.respondToFriendRequest(request)

        assertAll(
            { assertEquals(FriendshipStatus.ACCEPTED, result.status) },
            { assertEquals(friendship.createdAt, result.createdAt) }, // 생성일은 변경되지 않음
        )
    }

    @Test
    fun `respondToFriendRequest - success - handles inactive respondent correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "inactive-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "inactive-receiver@test.com",
            nickname = "receiver"
        )

        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        // 수신자를 비활성화 (실제로는 불가능하지만 테스트를 위해)
        receiver.deactivate()
        flushAndClear()

        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )

        assertFailsWith<FriendResponseException> {
            friendResponder.respondToFriendRequest(request)
        }.let {
            assertEquals("친구 요청 응답에 실패했습니다.", it.message)
        }
    }
}
