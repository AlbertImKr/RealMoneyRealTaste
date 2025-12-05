package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.exception.FriendResponseException
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class FriendResponderTest(
    private val friendResponder: FriendResponder,
    private val friendRequestor: FriendRequestor,
    private val friendshipReader: FriendshipReader,
    private val testMemberHelper: TestMemberHelper,
    private val friendshipRepository: FriendshipRepository,
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
        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
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
        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

    @Test
    fun `respondToFriendRequest - success - persists response to database correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "persist-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "persist-receiver@test.com",
            nickname = "receiver"
        )

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
        flushAndClear()

        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )
        val result = friendResponder.respondToFriendRequest(request)

        flushAndClear()

        val persisted = friendshipReader.findFriendshipById(result.requireId())
        assertAll(
            { assertNotNull(persisted) },
            { assertEquals(FriendshipStatus.ACCEPTED, persisted?.status) },
            { assertEquals(result.requireId(), persisted?.requireId()) },
            { assertTrue(persisted?.updatedAt!! > persisted.createdAt) }
        )
    }

    @Test
    fun `respondToFriendRequest - success - creates bidirectional friendship on acceptance`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "bidirectional-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "bidirectional-receiver@test.com",
            nickname = "receiver"
        )

        val originalFriendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

        val request = FriendResponseRequest(
            friendshipId = originalFriendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )
        friendResponder.respondToFriendRequest(request)

        flushAndClear()

        // 양방향 친구 관계 확인
        val forwardFriendship = friendshipReader.findActiveFriendship(sender.requireId(), receiver.requireId())!!
        val reverseFriendship = friendshipReader.findActiveFriendship(receiver.requireId(), sender.requireId())!!

        assertAll(
            { assertNotNull(forwardFriendship) },
            { assertNotNull(reverseFriendship) },
            { assertEquals(FriendshipStatus.ACCEPTED, forwardFriendship.status) },
            { assertEquals(FriendshipStatus.ACCEPTED, reverseFriendship.status) },
            { assertNotEquals(forwardFriendship.requireId(), reverseFriendship.requireId()) }
        )
    }

    @Test
    fun `respondToFriendRequest - success - does not create reverse friendship on rejection`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "noreverse-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "noreverse-receiver@test.com",
            nickname = "receiver"
        )

        val originalFriendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

        val request = FriendResponseRequest(
            friendshipId = originalFriendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = false
        )
        friendResponder.respondToFriendRequest(request)

        flushAndClear()

        // 원래 관계는 거절 상태, 역방향 관계는 없음
        val rejectedFriendship = friendshipReader.findFriendshipById(originalFriendship.requireId())
        val reverseFriendship = friendshipReader.findActiveFriendship(receiver.requireId(), sender.requireId())

        assertAll(
            { assertNotNull(rejectedFriendship) },
            { assertEquals(FriendshipStatus.REJECTED, rejectedFriendship.status) },
            { assertNull(reverseFriendship) }
        )
    }

    @Test
    fun `respondToFriendRequest - failure - throws exception when friendship is already unfriended`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "unfriend-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "unfriend-receiver@test.com",
            nickname = "receiver"
        )

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
        friendship.accept()

        // 친구 관계를 UNFRIENDED 상태로 변경
        friendship.unfriend()
        friendshipRepository.save(friendship)

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

    @Test
    fun `respondToFriendRequest - success - updates timestamps correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "timestamp-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "timestamp-receiver@test.com",
            nickname = "receiver"
        )

        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
        val originalUpdatedAt = friendship.updatedAt

        // 잠시 대기하여 시간 차이 보장
        Thread.sleep(100)

        val request = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = true
        )
        val result = friendResponder.respondToFriendRequest(request)

        assertAll(
            { assertEquals(friendship.createdAt, result.createdAt) }, // 생성일은 변경되지 않음
            { assertTrue(result.updatedAt > originalUpdatedAt) } // 수정일은 업데이트됨
        )
    }
}
