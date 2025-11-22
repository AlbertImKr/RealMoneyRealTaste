package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest
import com.albert.realmoneyrealtaste.application.friend.exception.UnfriendException
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RecordApplicationEvents
class FriendshipTerminatorTest(
    private val friendshipTerminator: FriendshipTerminator,
    private val friendRequestor: FriendRequestor,
    private val friendResponder: FriendResponder,
    private val friendshipReader: FriendshipReader,
    private val testMemberHelper: TestMemberHelper,
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
        val savedFriendShip = friendshipReader.sentedFriendRequest(
            member1.requireId(), member2.requireId()
        )
        val savedReverseFriendShip = friendshipReader.sentedFriendRequest(
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
        val savedFriendship = friendshipReader.sentedFriendRequest(
            member1.requireId(), member2.requireId()
        )
        val savedReverseFriendship = friendshipReader.sentedFriendRequest(
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
        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        friendRequestor.sendFriendRequest(command)

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

    private fun createAcceptedFriendship(fromMemberId: Long, toMemberId: Long): Friendship {
        // 친구 요청 생성
        val command = FriendRequestCommand(fromMemberId, toMemberId, "testUser")
        val friendship = friendRequestor.sendFriendRequest(command)

        // 친구 요청 수락
        val response = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = toMemberId,
            accept = true
        )
        return friendResponder.respondToFriendRequest(response)
    }
}
