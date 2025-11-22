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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

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
        val command = FriendRequestCommand(fromMember.requireId(), toMember.requireId(), toMember.nickname.value)
        applicationEvents.clear()

        // when
        val result = friendRequestor.sendFriendRequest(command)

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
        val command = FriendRequestCommand(fromMember.requireId(), toMember.requireId(), toMember.nickname.value)

        // when
        val result = friendRequestor.sendFriendRequest(command)

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
        val command = FriendRequestCommand(nonExistentMemberId, toMember.requireId(), toMember.nickname.value)

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(command)
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
        val command = FriendRequestCommand(fromMember.requireId(), nonExistentMemberId, "unknown")

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(command)
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when duplicate pending request exists`() {
        // given
        val fromMember = testMemberHelper.createActivatedMember(
            email = "duplicate-sender@test.com",
            nickname = "duplicatesender"
        )
        val toMember = testMemberHelper.createActivatedMember(
            email = "duplicate-receiver@test.com",
            nickname = "duplicatereceiver"
        )

        // 첫 번째 요청
        val command = FriendRequestCommand(fromMember.requireId(), toMember.requireId(), toMember.nickname.value)
        friendRequestor.sendFriendRequest(command)

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(command)
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when friendship already exists`() {
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
        val initialCommand = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(initialCommand)
        friendship.accept()
        friendshipRepository.save(friendship)

        // 이미 친구인 상태에서 다시 요청 시도
        val duplicateCommand = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(duplicateCommand)
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }

    @Test
    fun `sendFriendRequest - failure - throws exception when requesting to self`() {
        val member = testMemberHelper.createActivatedMember(
            email = "self@test.com",
            nickname = "self"
        )

        assertFailsWith<IllegalArgumentException> {
            FriendRequestCommand(member.requireId(), member.requireId(), member.nickname.value)
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
        val firstCommand = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        val firstFriendship = friendRequestor.sendFriendRequest(firstCommand)
        firstFriendship.reject()
        friendshipRepository.save(firstFriendship)

        // 반대 방향 요청 (허용되어야 함)
        val reverseCommand = FriendRequestCommand(member2.requireId(), member1.requireId(), member1.nickname.value)
        val result = friendRequestor.sendFriendRequest(reverseCommand)

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

        val command1 = FriendRequestCommand(sender.requireId(), receiver1.requireId(), receiver1.nickname.value)
        val command2 = FriendRequestCommand(sender.requireId(), receiver2.requireId(), receiver2.nickname.value)

        // when
        val result1 = friendRequestor.sendFriendRequest(command1)
        val result2 = friendRequestor.sendFriendRequest(command2)

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

        val command =
            FriendRequestCommand(activeMember.requireId(), inactiveMember.requireId(), inactiveMember.nickname.value)

        // when & then
        assertFailsWith<FriendRequestException> {
            friendRequestor.sendFriendRequest(command)
        }.let {
            assertEquals("친구 요청에 실패했습니다.", it.message)
        }
    }
}
