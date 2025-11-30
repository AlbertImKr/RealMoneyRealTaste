package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest
import com.albert.realmoneyrealtaste.application.friend.exception.UnfriendException
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class FriendshipTerminatorTest(
    private val friendshipTerminator: FriendshipTerminator,
    private val friendRequestor: FriendRequestor,
    private val friendResponder: FriendResponder,
    private val friendshipReader: FriendshipReader,
    private val testMemberHelper: TestMemberHelper,
    private val friendshipRepository: FriendshipRepository,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `unfriend - success - terminates friendship and publishes event`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "unfriend1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "unfriend2@test.com",
            nickname = "member2"
        )

        // 친구 관계 생성 (요청 -> 수락)
        createAcceptedFriendship(member1.requireId(), member2.requireId())
        applicationEvents.clear()

        // 친구 관계 해제
        val request = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(request)

        // 양방향 친구 관계 모두 해제되었는지 확인
        val savedFriendShip = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(), member2.requireId()
        )
        val savedReverseFriendShip = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member2.requireId(), member1.requireId()
        )
        val allFriendships = listOfNotNull(savedFriendShip, savedReverseFriendShip)

        val unfriendedfriendships = allFriendships.filter { it.status == FriendshipStatus.UNFRIENDED }
        assertEquals(2, unfriendedfriendships.size)

        // 이벤트 발행 확인
        val events = applicationEvents.stream(FriendshipTerminatedEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(member1.requireId(), event.memberId) },
            { assertEquals(member2.requireId(), event.friendMemberId) }
        )
    }

    @Test
    fun `unfriend - success - handles reverse direction correctly`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "reverse1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "reverse2@test.com",
            nickname = "member2"
        )

        // 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId())

        // 반대 방향에서 친구 관계 해제
        val request = UnfriendRequest(
            memberId = member2.requireId(),
            friendMemberId = member1.requireId()
        )
        friendshipTerminator.unfriend(request)

        // 양방향 친구 관계 모두 해제되었는지 확인
        val savedFriendship = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(), member2.requireId()
        )
        val savedReverseFriendship = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member2.requireId(), member1.requireId()
        )
        val allFriendships = listOfNotNull(savedFriendship, savedReverseFriendship)

        val unfriendedfriendships = allFriendships.filter { it.status == FriendshipStatus.UNFRIENDED }
        assertEquals(2, unfriendedfriendships.size)
    }

    @Test
    fun `unfriend - failure - throws exception when member does not exist`() {
        val member = testMemberHelper.createActivatedMember(
            email = "existing@test.com",
            nickname = "existing"
        )
        val nonExistentMemberId = 999999L

        val request = UnfriendRequest(
            memberId = nonExistentMemberId,
            friendMemberId = member.requireId()
        )

        assertFailsWith<UnfriendException> {
            friendshipTerminator.unfriend(request)
        }.let {
            assertEquals("친구 관계 해제에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfriend - success - handles non existent friendship gracefully`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "norelation1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "norelation2@test.com",
            nickname = "member2"
        )

        applicationEvents.clear()

        // 친구 관계가 없는 상태에서 해제 시도
        val request = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )

        // 예외 발생하지 않고 정상 처리 (null 체크로 인해)
        friendshipTerminator.unfriend(request)

        // 이벤트는 여전히 발행됨
        val events = applicationEvents.stream(FriendshipTerminatedEvent::class.java).toList()
        assertEquals(1, events.size)
    }

    @Test
    fun `unfriend - success - handles already terminated friendship`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "terminated1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "terminated2@test.com",
            nickname = "member2"
        )

        // 친구 관계 생성 후 이미 해제
        createAcceptedFriendship(member1.requireId(), member2.requireId())
        val firstRequest = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(firstRequest)
        applicationEvents.clear()

        // 이미 해제된 관계를 다시 해제 시도
        val secondRequest = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )

        friendshipTerminator.unfriend(secondRequest)
    }

    @Test
    fun `unfriend - failure - throws exception when member is not active`() {
        val activeMember = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )
        val inactiveMember = testMemberHelper.createMember(
            email = "inactive@test.com",
            nickname = "inactive"
        )
        // inactive member는 활성화하지 않음

        val request = UnfriendRequest(
            memberId = inactiveMember.requireId(),
            friendMemberId = activeMember.requireId()
        )

        assertFailsWith<UnfriendException> {
            friendshipTerminator.unfriend(request)
        }.let {
            assertEquals("친구 관계 해제에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfriend - success - updates friendship status and timestamp correctly`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "timestamp1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "timestamp2@test.com",
            nickname = "member2"
        )

        // 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId())

        // 원본 친구 관계 조회
        val originalFriendship = friendshipReader.findActiveFriendship(
            member1.requireId(), member2.requireId()
        )!!
        val originalUpdatedAt = originalFriendship.updatedAt

        // 친구 관계 해제
        val request = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(request)

        // 상태 및 타임스탬프 변경 확인
        val updatedFriendship = friendshipReader.findFriendshipById(originalFriendship.requireId())!!
        assertAll(
            { assertEquals(FriendshipStatus.UNFRIENDED, updatedFriendship.status) },
            { assertEquals(originalFriendship.createdAt, updatedFriendship.createdAt) },
            { assertTrue(updatedFriendship.updatedAt >= originalUpdatedAt) }
        )
    }

    @Test
    fun `unfriend - success - handles pending friendship correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "pending-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "pending-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청만 생성 (수락하지 않음)
        friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

        // PENDING 상태의 친구 요청에 대해 해제 시도
        val request = UnfriendRequest(
            memberId = sender.requireId(),
            friendMemberId = receiver.requireId()
        )

        // findActiveFriendship이 ACCEPTED 상태만 찾으므로 해제 대상이 없음
        // 정상적으로 처리됨 (null 체크)
        friendshipTerminator.unfriend(request)

        // 이벤트는 발행됨
        val events = applicationEvents.stream(FriendshipTerminatedEvent::class.java).toList()
        assertEquals(1, events.size)
    }

    @Test
    fun `unfriend - failure - throws exception when trying to unfriend self`() {
        val member = testMemberHelper.createActivatedMember(
            email = "selfunfriend@test.com",
            nickname = "selfunfriend"
        )

        val request = mockk<UnfriendRequest>()
        every { request.memberId } returns 1L
        every { request.friendMemberId } returns 1L


        assertFailsWith<UnfriendException> {
            friendshipTerminator.unfriend(request)
        }.let {
            assertEquals("친구 관계 해제에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `unfriend - success - handles rejected friendship correctly`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "rejected-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "rejected-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청 생성 후 거절
        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())
        val response = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = receiver.requireId(),
            accept = false
        )
        friendResponder.respondToFriendRequest(response)

        // REJECTED 상태의 친구 관계에 대해 해제 시도
        val request = UnfriendRequest(
            memberId = sender.requireId(),
            friendMemberId = receiver.requireId()
        )

        // findActiveFriendship이 ACCEPTED 상태만 찾으므로 해제 대상이 없음
        // 정상적으로 처리됨 (null 체크)
        friendshipTerminator.unfriend(request)

        // 이벤트는 발행됨
        val events = applicationEvents.stream(FriendshipTerminatedEvent::class.java).toList()
        assertEquals(1, events.size)
    }

    @Test
    fun `unfriend - success - handles multiple friendships termination`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "multi1@test.com",
            nickname = "multi1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "multi2@test.com",
            nickname = "multi2"
        )
        val member3 = testMemberHelper.createActivatedMember(
            email = "multi3@test.com",
            nickname = "multi3"
        )

        // 여러 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId())
        createAcceptedFriendship(member1.requireId(), member3.requireId())

        // member1과 member2의 친구 관계 해제
        val request1 = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(request1)

        // member1과 member3의 친구 관계는 유지되어야 함
        val activeFriendship = friendshipReader.findActiveFriendship(member1.requireId(), member3.requireId())
        assertNotNull(activeFriendship)
        assertEquals(FriendshipStatus.ACCEPTED, activeFriendship.status)

        // member1과 member2의 관계는 해제되어야 함
        val terminatedFriendship = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(),
            member2.requireId()
        )

        assertNotNull(terminatedFriendship)
        assertEquals(FriendshipStatus.UNFRIENDED, terminatedFriendship.status)
    }

    @Test
    fun `unfriend - success - persists termination to database`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "persist1@test.com",
            nickname = "persist1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "persist2@test.com",
            nickname = "persist2"
        )

        // 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId())
        flushAndClear()

        // 친구 관계 해제
        val request = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(request)

        flushAndClear()

        // 데이터베이스에서 상태 확인
        val friendship1 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(),
            member2.requireId()
        )
        val friendship2 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member2.requireId(),
            member1.requireId()
        )

        assertNotNull(friendship1)
        assertNotNull(friendship2)
        assertEquals(FriendshipStatus.UNFRIENDED, friendship1.status)
        assertEquals(FriendshipStatus.UNFRIENDED, friendship2.status)
    }

    @Test
    fun `unfriend - success - does not affect other friendships`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "isolate1@test.com",
            nickname = "isolate1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "isolate2@test.com",
            nickname = "isolate2"
        )
        val member3 = testMemberHelper.createActivatedMember(
            email = "isolate3@test.com",
            nickname = "isolate3"
        )
        val member4 = testMemberHelper.createActivatedMember(
            email = "isolate4@test.com",
            nickname = "isolate4"
        )

        // 여러 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId())
        createAcceptedFriendship(member1.requireId(), member3.requireId())
        createAcceptedFriendship(member2.requireId(), member4.requireId())

        // member1과 member2의 관계만 해제
        val request = UnfriendRequest(
            memberId = member1.requireId(),
            friendMemberId = member2.requireId()
        )
        friendshipTerminator.unfriend(request)

        // 다른 관계들은 영향받지 않아야 함
        val friendship1And3 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(),
            member3.requireId()
        )
        val friendship2And4 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member2.requireId(),
            member4.requireId()
        )

        assertNotNull(friendship1And3)
        assertNotNull(friendship2And4)
        assertEquals(FriendshipStatus.ACCEPTED, friendship1And3.status)
        assertEquals(FriendshipStatus.ACCEPTED, friendship2And4.status)

        // 해제된 관계 확인
        val terminated1And2 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member1.requireId(),
            member2.requireId(),
        )
        val terminated2And1 = friendshipRepository.findByRelationShipMemberIdAndRelationShipFriendMemberId(
            member2.requireId(),
            member1.requireId(),
        )
        assertNotNull(terminated1And2)
        assertNotNull(terminated2And1)
        assertEquals(FriendshipStatus.UNFRIENDED, terminated1And2.status)
        assertEquals(FriendshipStatus.UNFRIENDED, terminated2And1.status)
    }

    private fun createAcceptedFriendship(fromMemberId: Long, toMemberId: Long): Friendship {
        // 친구 요청 생성
        val friendship = friendRequestor.sendFriendRequest(fromMemberId, toMemberId)

        // 친구 요청 수락
        val response = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = toMemberId,
            accept = true
        )
        return friendResponder.respondToFriendRequest(response)
    }
}
