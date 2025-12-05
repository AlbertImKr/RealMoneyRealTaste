package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.exception.FriendRequestException
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RecordApplicationEvents
class FriendRequestorTest(
    private val friendRequestor: FriendRequestor,
    private val friendshipRepository: FriendshipRepository,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `sendFriendRequest - success - creates friendship and publishes event when valid request`() {
        // given
        val fromMember = testMemberHelper.createActivatedMember(
            email = "sender@test.com",
            nickname = "sender"
        )
        val toMember = testMemberHelper.createActivatedMember(
            email = "receiver@test.com",
            nickname = "receiver"
        )
        applicationEvents.clear()

        // when
        val result = friendRequestor.sendFriendRequest(fromMember.requireId(), toMember.requireId())

        // then
        assertAll(
            { assertNotNull(result.id) },
            { assertEquals(fromMember.requireId(), result.relationShip.memberId) },
            { assertEquals(toMember.requireId(), result.relationShip.friendMemberId) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) },
            { assertEquals(result.createdAt, result.updatedAt) }
        )

        // Event 발행 확인
        val events = applicationEvents.stream(FriendRequestSentEvent::class.java).toList()
        assertEquals(1, events.size)
        val event = events.first()
        assertAll(
            { assertEquals(result.requireId(), event.friendshipId) },
            { assertEquals(fromMember.requireId(), event.fromMemberId) },
            { assertEquals(toMember.requireId(), event.toMemberId) }
        )
    }

    @Test
    fun `sendFriendRequest - success - saves friendship to database`() {
        // given
        val fromMember = testMemberHelper.createActivatedMember(
            email = "db-sender@test.com",
            nickname = "dbsender"
        )
        val toMember = testMemberHelper.createActivatedMember(
            email = "db-receiver@test.com",
            nickname = "dbreceiver"
        )

        // when
        val result = friendRequestor.sendFriendRequest(fromMember.requireId(), toMember.requireId())

        // then
        val saved = friendshipRepository.findById(result.requireId())
        assertAll(
            { assertNotNull(saved) },
            { assertEquals(fromMember.requireId(), saved?.relationShip?.memberId) },
            { assertEquals(toMember.requireId(), saved?.relationShip?.friendMemberId) },
            { assertEquals(FriendshipStatus.PENDING, saved?.status) }
        )
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when from member does not exist`() {
        // given
        val nonExistentMemberId = 999999L
        val toMember = testMemberHelper.createActivatedMember(
            email = "existing@test.com",
            nickname = "existing"
        )

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(nonExistentMemberId, toMember.requireId())
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when to member does not exist`() {
        // given
        val fromMember = testMemberHelper.createActivatedMember(
            email = "existing2@test.com",
            nickname = "existing2"
        )
        val nonExistentMemberId = 999999L

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(fromMember.requireId(), nonExistentMemberId)
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - success - return existing friendship when friendship already exists`() {
        // given
        val member1 = testMemberHelper.createActivatedMember(
            email = "friend1@test.com",
            nickname = "friend1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "friend2@test.com",
            nickname = "friend2"
        )

        // 친구 요청 생성 후 수락
        val friendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())
        friendship.accept()
        friendshipRepository.save(friendship)

        // when & then
        val result = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())

        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(friendship.relationShip.memberId, result.relationShip.memberId) },
            { assertEquals(friendship.relationShip.friendMemberId, result.relationShip.friendMemberId) },
            { assertEquals(friendship.status, result.status) }
        )
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when requesting to self`() {
        val member = testMemberHelper.createActivatedMember(
            email = "self@test.com",
            nickname = "self"
        )

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                member.requireId(),
                member.nickname.value,
                member.profileImageId,
                member.requireId(),
                member.nickname.value,
                member.profileImageId,
            )
        }
    }

    @Test
    fun `sendFriendRequest - success - allows reverse direction after rejection`() {
        // given
        val member1 = testMemberHelper.createActivatedMember(
            email = "reverse1@test.com",
            nickname = "reverse1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "reverse2@test.com",
            nickname = "reverse2"
        )

        // 첫 번째 요청 후 거절
        val firstFriendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())
        firstFriendship.reject()
        friendshipRepository.save(firstFriendship)

        // 반대 방향 요청 (허용되어야 함)
        val result = friendRequestor.sendFriendRequest(member2.requireId(), member1.requireId())

        assertAll(
            { assertNotNull(result) },
            { assertEquals(member2.requireId(), result.relationShip.memberId) },
            { assertEquals(member1.requireId(), result.relationShip.friendMemberId) },
            { assertEquals(FriendshipStatus.PENDING, result.status) }
        )
    }

    @Test
    fun `sendFriendRequest - success - handles multiple friend requests correctly`() {
        // given
        val sender = testMemberHelper.createActivatedMember(
            email = "multi-sender@test.com",
            nickname = "multisender"
        )
        val receiver1 = testMemberHelper.createActivatedMember(
            email = "receiver1@test.com",
            nickname = "receiver1"
        )
        val receiver2 = testMemberHelper.createActivatedMember(
            email = "receiver2@test.com",
            nickname = "receiver2"
        )

        // when
        val result1 = friendRequestor.sendFriendRequest(sender.requireId(), receiver1.requireId())
        val result2 = friendRequestor.sendFriendRequest(sender.requireId(), receiver2.requireId())

        // then
        assertAll(
            { assertEquals(sender.requireId(), result1.relationShip.memberId) },
            { assertEquals(receiver1.requireId(), result1.relationShip.friendMemberId) },
            { assertEquals(sender.requireId(), result2.relationShip.memberId) },
            { assertEquals(receiver2.requireId(), result2.relationShip.friendMemberId) },
            { assertNotEquals(result1.requireId(), result2.requireId()) }
        )
    }

    @Test
    fun `sendFriendRequest - success - validates member status is active`() {
        // given
        val activeMember = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )

        // 비활성 회원 생성 시도 시 MemberReader.readActiveMemberById에서 예외 발생
        val inactiveMember = testMemberHelper.createMember()
        // 활성화하지 않음 (PENDING 상태)

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(activeMember.requireId(), inactiveMember.requireId())
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - success - creates friendship with correct timestamps`() {
        val fromMember = testMemberHelper.createActivatedMember(
            email = "timestamp-from@test.com",
            nickname = "frommember"
        )
        val toMember = testMemberHelper.createActivatedMember(
            email = "timestamp-to@test.com",
            nickname = "tomember"
        )

        val beforeCreation = LocalDateTime.now()

        val result = friendRequestor.sendFriendRequest(fromMember.requireId(), toMember.requireId())

        val afterCreation = LocalDateTime.now()

        assertAll(
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) },
            { assertEquals(result.createdAt, result.updatedAt) },
            { assertTrue(result.createdAt.isAfter(beforeCreation.minusSeconds(1))) },
            { assertTrue(result.createdAt.isBefore(afterCreation.plusSeconds(1))) }
        )
    }

    @Test
    fun `sendFriendRequest - success - persists friendship to database correctly`() {
        val fromMember = testMemberHelper.createActivatedMember(
            email = "persist-from@test.com",
            nickname = "frommember"
        )
        val toMember = testMemberHelper.createActivatedMember(
            email = "persist-to@test.com",
            nickname = "tomember"
        )

        val result = friendRequestor.sendFriendRequest(fromMember.requireId(), toMember.requireId())

        flushAndClear()

        val persisted = friendshipRepository.findById(result.requireId())
        assertAll(
            { assertNotNull(persisted) },
            { assertEquals(result.requireId(), persisted?.requireId()) },
            { assertEquals(FriendshipStatus.PENDING, persisted?.status) },
            { assertEquals(fromMember.requireId(), persisted?.relationShip?.memberId) },
            { assertEquals(toMember.requireId(), persisted?.relationShip?.friendMemberId) }
        )
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when both members are the same`() {
        val member = testMemberHelper.createActivatedMember(
            email = "same@test.com",
            nickname = "same"
        )

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(
                member.requireId(),
                member.nickname.value,
                member.profileImageId,
                member.requireId(),
                member.nickname.value,
                member.profileImageId,
            )
        }
    }

    @Test
    fun `sendFriendRequest - success - allows request after rejection`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "after-rejection1@test.com",
            nickname = "member1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "after-rejection2@test.com",
            nickname = "member2"
        )

        // 첫 번째 요청 후 거절
        val firstFriendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())
        firstFriendship.reject()
        friendshipRepository.save(firstFriendship)

        // 거절된 후 동일 방향으로 다시 요청 (허용되어야 함)
        val secondCommand = FriendRequestCommand(
            member1.requireId(),
            member1.nickname.value,
            member1.profileImageId,
            member2.requireId(),
            member2.nickname.value,
            member2.profileImageId,
        )
        val result = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())

        assertAll(
            { assertNotNull(result) },
            { assertEquals(member1.requireId(), result.relationShip.memberId) },
            { assertEquals(member2.requireId(), result.relationShip.friendMemberId) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertEquals(firstFriendship.requireId(), result.requireId()) }
        )
    }
}
