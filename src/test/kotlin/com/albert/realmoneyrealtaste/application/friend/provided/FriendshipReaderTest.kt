package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.exception.FriendshipNotFoundException
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.junit.jupiter.api.assertAll
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FriendshipReaderTest(
    private val friendshipReader: FriendshipReader,
    private val friendRequestor: FriendRequestor,
    private val friendResponder: FriendResponder,
    private val testMemberHelper: TestMemberHelper,
) : IntegrationTestBase() {

    @Test
    fun `findActiveFriendship - success - returns friendship when exists and accepted`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "active1@test.com",
            nickname = "active1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "active2@test.com",
            nickname = "active2"
        )

        // 친구 관계 생성
        createAcceptedFriendship(member1.requireId(), member2.requireId(), member2.nickname.value)

        val result = friendshipReader.findActiveFriendship(member1.requireId(), member2.requireId())

        assertNotNull(result)
        assertAll(
            { assertEquals(member1.requireId(), result.relationShip.memberId) },
            { assertEquals(member2.requireId(), result.relationShip.friendMemberId) },
            { assertEquals(FriendshipStatus.ACCEPTED, result.status) }
        )
    }

    @Test
    fun `findActiveFriendship - success - returns null when friendship is pending`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "pending1@test.com",
            nickname = "pending1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "pending2@test.com",
            nickname = "pending2"
        )

        // 친구 요청만 생성 (수락하지 않음)
        val command = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        friendRequestor.sendFriendRequest(command)

        val result = friendshipReader.findActiveFriendship(member1.requireId(), member2.requireId())

        assertNull(result)
    }

    @Test
    fun `findActiveFriendship - success - returns null when friendship does not exist`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "norelation1@test.com",
            nickname = "norelation1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "norelation2@test.com",
            nickname = "norelation2"
        )

        val result = friendshipReader.findActiveFriendship(member1.requireId(), member2.requireId())

        assertNull(result)
    }

    @Test
    fun `findFriendshipBetweenMembers - success - returns friendship regardless of status`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "between1@test.com",
            nickname = "between1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "between2@test.com",
            nickname = "between2"
        )

        // 친구 요청 생성 (PENDING 상태)
        val command = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        val result = friendshipReader.sentedFriendRequest(member1.requireId(), member2.requireId())

        assertNotNull(result)
        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(FriendshipStatus.PENDING, result.status) }
        )
    }

    @Test
    fun `findPendingFriendshipReceived - success - returns pending request received by member`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "received-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "received-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청 생성
        val command = FriendRequestCommand(sender.requireId(), receiver.requireId(), receiver.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        val result = friendshipReader.findPendingFriendshipReceived(receiver.requireId(), sender.requireId())

        assertNotNull(result)
        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertTrue(result.isReceivedBy(receiver.requireId())) }
        )
    }

    @Test
    fun `findPendingFriendshipReceived - success - returns null when no pending request`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "nopending1@test.com",
            nickname = "nopending1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "nopending2@test.com",
            nickname = "nopending2"
        )

        val result = friendshipReader.findPendingFriendshipReceived(member1.requireId(), member2.requireId())

        assertNull(result)
    }

    @Test
    fun `findFriendshipById - success - returns friendship when exists`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "byid1@test.com",
            nickname = "byid1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "byid2@test.com",
            nickname = "byid2"
        )

        val command = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        val friendship = friendRequestor.sendFriendRequest(command)

        val result = friendshipReader.findFriendshipById(friendship.requireId())

        assertAll(
            { assertEquals(friendship.requireId(), result.requireId()) },
            { assertEquals(member1.requireId(), result.relationShip.memberId) },
            { assertEquals(member2.requireId(), result.relationShip.friendMemberId) }
        )
    }

    @Test
    fun `findFriendshipById - failure - throws exception when friendship does not exist`() {
        val nonExistentId = 999999L

        assertFailsWith<FriendshipNotFoundException> {
            friendshipReader.findFriendshipById(nonExistentId)
        }.let {
            assertEquals("친구 관계를 찾을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `findFriendsByMemberId - success - returns empty page when no friends`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nofriends@test.com",
            nickname = "lonely"
        )

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findFriendsByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertTrue(result.isEmpty) }
        )
    }

    @Test
    fun `findPendingRequestsReceived - success - returns pending requests received by member`() {
        val receiver = testMemberHelper.createActivatedMember(
            email = "pending-receiver@test.com",
            nickname = "receiver"
        )
        val sender1 = testMemberHelper.createActivatedMember(
            email = "sender1@test.com",
            nickname = "sender1"
        )
        val sender2 = testMemberHelper.createActivatedMember(
            email = "sender2@test.com",
            nickname = "sender2"
        )

        // 두 개의 친구 요청 생성
        val command1 = FriendRequestCommand(sender1.requireId(), receiver.requireId(), receiver.nickname.value)
        val command2 = FriendRequestCommand(sender2.requireId(), receiver.requireId(), receiver.nickname.value)
        friendRequestor.sendFriendRequest(command1)
        friendRequestor.sendFriendRequest(command2)

        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())
        val result = friendshipReader.findPendingRequestsReceived(receiver.requireId(), pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertFalse(result.isEmpty) },
            { assertTrue(result.content.all { it.friendNickname == receiver.nickname.value }) },
            { assertTrue(result.content.all { it.status == FriendshipStatus.PENDING }) }
        )
    }

    @Test
    fun `findPendingRequestsSent - success - returns pending requests sent by member`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "pending-sender@test.com",
            nickname = "sender"
        )
        val receiver1 = testMemberHelper.createActivatedMember(
            email = "receiver1@test.com",
            nickname = "receiver1"
        )
        val receiver2 = testMemberHelper.createActivatedMember(
            email = "receiver2@test.com",
            nickname = "receiver2"
        )

        // 두 개의 친구 요청 생성
        val command1 = FriendRequestCommand(sender.requireId(), receiver1.requireId(), receiver1.nickname.value)
        val command2 = FriendRequestCommand(sender.requireId(), receiver2.requireId(), receiver2.nickname.value)
        friendRequestor.sendFriendRequest(command1)
        friendRequestor.sendFriendRequest(command2)

        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())
        val result = friendshipReader.findPendingRequestsSent(sender.requireId(), pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertFalse(result.isEmpty) },
            { assertTrue(result.content.all { it.status == FriendshipStatus.PENDING }) }
        )
    }

    @Test
    fun `existsByMemberIds - success - returns true when friendship exists`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "exists1@test.com",
            nickname = "exists1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "exists2@test.com",
            nickname = "exists2"
        )

        val command = FriendRequestCommand(member1.requireId(), member2.requireId(), member2.nickname.value)
        friendRequestor.sendFriendRequest(command)

        val result = friendshipReader.isFriend(member1.requireId(), member2.requireId())

        assertTrue(result)
    }

    @Test
    fun `existsByMemberIds - success - returns false when friendship does not exist`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "notexists1@test.com",
            nickname = "notexists1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "notexists2@test.com",
            nickname = "notexists2"
        )

        val result = friendshipReader.isFriend(member1.requireId(), member2.requireId())

        assertFalse(result)
    }

    @Test
    fun `findFriendsByMemberId - success - handles pagination correctly`() {
        val member = testMemberHelper.createActivatedMember(
            email = "pagination-member@test.com",
            nickname = "member"
        )

        // 5개의 친구 관계 생성
        val friends = (1..5).map { index ->
            testMemberHelper.createActivatedMember(
                email = "pagefriend$index@test.com",
                nickname = "pagefriend$index"
            )
        }

        friends.forEach { friend ->
            createAcceptedFriendship(friend.requireId(), member.requireId(), member.nickname.value)
        }

        // 페이지 크기 2로 첫 번째 페이지 조회
        val firstPage = PageRequest.of(0, 2, Sort.by("createdAt").ascending())
        val firstResult = friendshipReader.findFriendsByMemberId(member.requireId(), firstPage)

        // 두 번째 페이지 조회
        val secondPage = PageRequest.of(1, 2, Sort.by("createdAt").ascending())
        val secondResult = friendshipReader.findFriendsByMemberId(member.requireId(), secondPage)

        assertAll(
            { assertEquals(5, firstResult.totalElements) },
            { assertEquals(2, firstResult.content.size) },
            { assertEquals(5, secondResult.totalElements) },
            { assertEquals(2, secondResult.content.size) },
            { assertTrue(firstResult.hasNext()) },
            { assertTrue(secondResult.hasNext()) }
        )
    }

    @Test
    fun `findFriendsByMemberId - success - excludes non-accepted friendships`() {
        val member = testMemberHelper.createActivatedMember(
            email = "exclude-member@test.com",
            nickname = "member"
        )
        val friend1 = testMemberHelper.createActivatedMember(
            email = "accepted-friend@test.com",
            nickname = "accepted"
        )
        val friend2 = testMemberHelper.createActivatedMember(
            email = "pending-friend@test.com",
            nickname = "pending"
        )

        // 하나는 수락, 하나는 대기 상태로 유지
        createAcceptedFriendship(friend1.requireId(), member.requireId(), member.nickname.value)
        val command2 = FriendRequestCommand(friend2.requireId(), member.requireId(), member.nickname.value)
        friendRequestor.sendFriendRequest(command2)

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findFriendsByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(FriendshipStatus.ACCEPTED, result.content.first().status) }
        )
    }

    private fun createAcceptedFriendship(fromMemberId: Long, toMemberId: Long, toMemberNickname: String): Friendship {
        val command = FriendRequestCommand(fromMemberId, toMemberId, toMemberNickname)
        val friendship = friendRequestor.sendFriendRequest(command)

        val response = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = toMemberId,
            accept = true
        )
        return friendResponder.respondToFriendRequest(response)
    }

    @Test
    fun `mapToFriendshipResponses - success - handles all unknown members gracefully`() {
        val member = testMemberHelper.createActivatedMember(
            email = "known-member@test.com",
            nickname = "member"
        )
        val activeFriend = testMemberHelper.createActivatedMember(
            email = "activeFriend@test.com",
            nickname = "activeFriend"
        )
        val deactivateFriend1 = testMemberHelper.createActivatedMember(
            email = "deactivateFriend1@test.com",
            nickname = "deactivate1",
        )
        val deactivateFriend2 = testMemberHelper.createActivatedMember(
            email = "deactivateFriend2@test.com",
            nickname = "deactivate2",
        )

        createAcceptedFriendship(activeFriend.requireId(), member.requireId(), member.nickname.value)
        createAcceptedFriendship(deactivateFriend1.requireId(), member.requireId(), member.nickname.value)
        createAcceptedFriendship(deactivateFriend2.requireId(), member.requireId(), member.nickname.value)

        // 친구들을 비활성화
        deactivateFriend1.deactivate()
        deactivateFriend2.deactivate()
        member.deactivate()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findFriendsByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(3, result.totalElements) },
            { assertEquals(3, result.content.size) },
            { assertEquals(result.content.count { it.friendMemberId == activeFriend.requireId() }, 1) },
            { assertEquals(result.content.count { it.friendMemberId == deactivateFriend1.requireId() }, 1) },
            { assertEquals(result.content.count { it.friendMemberId == deactivateFriend2.requireId() }, 1) },
        )
    }

    @Test
    fun `countFriendsByMemberId - success - returns correct friend count`() {
        val member = testMemberHelper.createActivatedMember(
            email = "count-member@test.com",
            nickname = "member"
        )
        val friends = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "countfriend$index@test.com",
                nickname = "countfriend$index"
            )
        }

        friends.forEach { friend ->
            createAcceptedFriendship(friend.requireId(), member.requireId(), member.nickname.value)
        }

        val result = friendshipReader.countFriendsByMemberId(member.requireId())

        assertEquals(friends.size.toLong(), result)
    }

    @Test
    fun `countFriendsByMemberId - success - returns zero when no friends`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nocount@test.com",
            nickname = "nocount"
        )

        val result = friendshipReader.countFriendsByMemberId(member.requireId())

        assertEquals(0L, result)
    }

    @Test
    fun `countFriendsByMemberId - success - excludes pending friendships`() {
        val member = testMemberHelper.createActivatedMember(
            email = "excludecount@test.com",
            nickname = "member"
        )
        val friend1 = testMemberHelper.createActivatedMember(
            email = "acceptedcount@test.com",
            nickname = "accepted"
        )
        val friend2 = testMemberHelper.createActivatedMember(
            email = "pendingcount@test.com",
            nickname = "pending"
        )

        // 하나는 수락, 하나는 대기 상태
        createAcceptedFriendship(friend1.requireId(), member.requireId(), member.nickname.value)
        val command2 = FriendRequestCommand(friend2.requireId(), member.requireId(), member.nickname.value)
        friendRequestor.sendFriendRequest(command2)

        val result = friendshipReader.countFriendsByMemberId(member.requireId())

        assertEquals(1L, result) // 수락된 친구만 카운트
    }

    @Test
    fun `searchFriends - success - returns friends matching keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "search-member@test.com",
            nickname = "member"
        )
        val targetFriend1 = testMemberHelper.createActivatedMember(
            email = "target1@test.com",
            nickname = "searchable"
        )
        val targetFriend2 = testMemberHelper.createActivatedMember(
            email = "target2@test.com",
            nickname = "searchable2"
        )
        val otherFriend = testMemberHelper.createActivatedMember(
            email = "other@test.com",
            nickname = "other"
        )

        createAcceptedFriendship(targetFriend1.requireId(), member.requireId(), member.nickname.value)
        createAcceptedFriendship(targetFriend2.requireId(), member.requireId(), member.nickname.value)
        createAcceptedFriendship(otherFriend.requireId(), member.requireId(), member.nickname.value)
        val result1 = friendshipReader.findFriendsByMemberId(member.requireId(), PageRequest.of(0, 10))

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.searchFriends(member.requireId(), "search", pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.all { it.friendNickname.contains("search") }) },
            { assertFalse(result.content.any { it.friendNickname == "other" }) }
        )
    }

    @Test
    fun `searchFriends - success - returns empty when no friends match keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nomatch-member@test.com",
            nickname = "member"
        )
        val friend = testMemberHelper.createActivatedMember(
            email = "nomatch@test.com",
            nickname = "friend"
        )

        createAcceptedFriendship(friend.requireId(), member.requireId(), member.nickname.value)

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.searchFriends(member.requireId(), "nonexistent", pageable)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `searchFriends - success - handles empty keyword`() {
        val member = testMemberHelper.createActivatedMember(
            email = "emptykeyword@test.com",
            nickname = "member"
        )
        val friends = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "emptyfriend$index@test.com",
                nickname = "emptyfriend$index"
            )
        }

        friends.forEach { friend ->
            createAcceptedFriendship(friend.requireId(), member.requireId(), member.nickname.value)
        }

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.searchFriends(member.requireId(), "", pageable)

        assertEquals(friends.size.toLong(), result.totalElements)
    }

    @Test
    fun `findRecentFriends - success - returns friends ordered by creation date`() {
        val member = testMemberHelper.createActivatedMember(
            email = "recent-member@test.com",
            nickname = "member"
        )

        // 시간 차이를 두고 친구 생성
        val friend1 = testMemberHelper.createActivatedMember(
            email = "recent1@test.com",
            nickname = "recent1"
        )
        val friend2 = testMemberHelper.createActivatedMember(
            email = "recent2@test.com",
            nickname = "recent2"
        )
        val friend3 = testMemberHelper.createActivatedMember(
            email = "recent3@test.com",
            nickname = "recent3"
        )

        createAcceptedFriendship(friend1.requireId(), member.requireId(), member.nickname.value)
        Thread.sleep(100) // 시간 차이 보장
        createAcceptedFriendship(friend2.requireId(), member.requireId(), member.nickname.value)
        Thread.sleep(100)
        createAcceptedFriendship(friend3.requireId(), member.requireId(), member.nickname.value)

        val pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending())
        val result = friendshipReader.findRecentFriends(member.requireId(), pageable)

        assertAll(
            { assertEquals(3, result.totalElements) },
            { assertEquals(2, result.content.size) },
            { assertEquals(friend3.requireId(), result.content[0].friendMemberId) }, // 가장 최근
            { assertEquals(friend2.requireId(), result.content[1].friendMemberId) }
        )
    }

    @Test
    fun `findRecentFriends - success - returns empty when no friends`() {
        val member = testMemberHelper.createActivatedMember(
            email = "norecent@test.com",
            nickname = "norecent"
        )

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findRecentFriends(member.requireId(), pageable)

        assertTrue(result.isEmpty)
        assertEquals(0, result.totalElements)
    }

    @Test
    fun `countPendingRequests - success - returns correct pending request count`() {
        val member = testMemberHelper.createActivatedMember(
            email = "pendingcount-member@test.com",
            nickname = "member"
        )
        val senders = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "sender$index@test.com",
                nickname = "sender$index"
            )
        }

        // 받은 친구 요청 생성
        senders.forEach { sender ->
            val command = FriendRequestCommand(sender.requireId(), member.requireId(), member.nickname.value)
            friendRequestor.sendFriendRequest(command)
        }

        val result = friendshipReader.countPendingRequests(member.requireId())

        assertEquals(senders.size.toLong(), result)
    }

    @Test
    fun `countPendingRequests - success - returns zero when no pending requests`() {
        val member = testMemberHelper.createActivatedMember(
            email = "nopendingcount@test.com",
            nickname = "nopending"
        )

        val result = friendshipReader.countPendingRequests(member.requireId())

        assertEquals(0L, result)
    }

    @Test
    fun `countPendingRequests - success - excludes accepted friendships`() {
        val member = testMemberHelper.createActivatedMember(
            email = "excludepending@test.com",
            nickname = "member"
        )
        val sender1 = testMemberHelper.createActivatedMember(
            email = "pendingsender@test.com",
            nickname = "pendingsender"
        )
        val sender2 = testMemberHelper.createActivatedMember(
            email = "acceptedsender@test.com",
            nickname = "acceptedsender"
        )

        // 하나는 대기 상태, 하나는 수락
        val command1 = FriendRequestCommand(sender1.requireId(), member.requireId(), member.nickname.value)
        friendRequestor.sendFriendRequest(command1)
        createAcceptedFriendship(sender2.requireId(), member.requireId(), member.nickname.value)

        val result = friendshipReader.countPendingRequests(member.requireId())

        assertEquals(1L, result) // 대기 중인 요청만 카운트
    }

    @Test
    fun `findPendingRequests - success - returns pending friendships as entities`() {
        val member = testMemberHelper.createActivatedMember(
            email = "pendingentity@test.com",
            nickname = "member"
        )
        val senders = (1..3).map { index ->
            testMemberHelper.createActivatedMember(
                email = "entitysender$index@test.com",
                nickname = "entitysender$index"
            )
        }

        val friendships = senders.map { sender ->
            val command = FriendRequestCommand(sender.requireId(), member.requireId(), member.nickname.value)
            friendRequestor.sendFriendRequest(command)
        }

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findPendingRequests(member.requireId(), pageable)

        assertAll(
            { assertEquals(friendships.size.toLong(), result.totalElements) },
            { assertEquals(friendships.size, result.content.size) },
            { assertTrue(result.content.all { it.status == FriendshipStatus.PENDING }) },
            {
                assertTrue(result.content.all { friendship ->
                    friendships.any { it.requireId() == friendship.requireId() }
                })
            }
        )
    }

    @Test
    fun `findByMembersId - success - returns null when no relationship exists`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "norelation1@test.com",
            nickname = "norelation1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "norelation2@test.com",
            nickname = "norelation2"
        )

        val result = friendshipReader.findByMembersId(member1.requireId(), member2.requireId())

        assertNull(result)
    }
}
