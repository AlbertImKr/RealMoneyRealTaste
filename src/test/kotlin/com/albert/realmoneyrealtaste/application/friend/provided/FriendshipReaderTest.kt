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
        createAcceptedFriendship(
            member1.requireId(),
            member1.nickname.value,
            member2.requireId(),
            member2.nickname.value
        )

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
        friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())

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
        val friendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())

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
        val friendship = friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())

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
        friendRequestor.sendFriendRequest(sender1.requireId(), receiver.requireId())
        friendRequestor.sendFriendRequest(sender2.requireId(), receiver.requireId())

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
        friendRequestor.sendFriendRequest(sender.requireId(), receiver1.requireId())
        friendRequestor.sendFriendRequest(sender.requireId(), receiver2.requireId())

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

        val friendship = friendRequestor.sendFriendRequest(member1.requireId(), member2.requireId())
        val friendResponseRequest = FriendResponseRequest(friendship.requireId(), member2.requireId(), true)
        friendResponder.respondToFriendRequest(friendResponseRequest)

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
            createAcceptedFriendship(
                friend.requireId(),
                friend.nickname.value,
                member.requireId(),
                member.nickname.value
            )
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
        createAcceptedFriendship(friend1.requireId(), friend1.nickname.value, member.requireId(), member.nickname.value)
        friendRequestor.sendFriendRequest(friend2.requireId(), member.requireId())

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findFriendsByMemberId(member.requireId(), pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(FriendshipStatus.ACCEPTED, result.content.first().status) }
        )
    }

    private fun createAcceptedFriendship(
        fromMemberId: Long,
        fromMemberNickname: String,
        toMemberId: Long,
        toMemberNickname: String,
    ): Friendship {
        FriendRequestCommand(fromMemberId, fromMemberNickname, toMemberId, toMemberNickname)
        val friendship = friendRequestor.sendFriendRequest(fromMemberId, toMemberId)

        val response = FriendResponseRequest(
            friendshipId = friendship.requireId(),
            respondentMemberId = toMemberId,
            accept = true
        )
        return friendResponder.respondToFriendRequest(response)
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
            createAcceptedFriendship(
                friend.requireId(),
                friend.nickname.value,
                member.requireId(),
                member.nickname.value
            )
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
        createAcceptedFriendship(friend1.requireId(), friend1.nickname.value, member.requireId(), member.nickname.value)
        friendRequestor.sendFriendRequest(friend2.requireId(), member.requireId())

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

        createAcceptedFriendship(
            targetFriend1.requireId(),
            targetFriend1.nickname.value,
            member.requireId(),
            member.nickname.value
        )
        createAcceptedFriendship(
            targetFriend2.requireId(),
            targetFriend1.nickname.value,
            member.requireId(),
            member.nickname.value
        )
        createAcceptedFriendship(
            otherFriend.requireId(),
            otherFriend.nickname.value,
            member.requireId(),
            member.nickname.value
        )
        friendshipReader.findFriendsByMemberId(member.requireId(), PageRequest.of(0, 10))

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

        createAcceptedFriendship(friend.requireId(), friend.nickname.value, member.requireId(), member.nickname.value)

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
            createAcceptedFriendship(
                friend.requireId(),
                friend.nickname.value,
                member.requireId(),
                member.nickname.value
            )
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

        createAcceptedFriendship(friend1.requireId(), friend1.nickname.value, member.requireId(), member.nickname.value)
        Thread.sleep(100) // 시간 차이 보장
        createAcceptedFriendship(friend2.requireId(), friend2.nickname.value, member.requireId(), member.nickname.value)
        Thread.sleep(100)
        createAcceptedFriendship(friend3.requireId(), friend3.nickname.value, member.requireId(), member.nickname.value)

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
            friendRequestor.sendFriendRequest(sender.requireId(), member.requireId())
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
        friendRequestor.sendFriendRequest(sender1.requireId(), member.requireId())
        createAcceptedFriendship(sender2.requireId(), sender2.nickname.value, member.requireId(), member.nickname.value)

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
            friendRequestor.sendFriendRequest(sender.requireId(), member.requireId())
        }

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findPendingRequests(member.requireId(), pageable)

        assertAll(
            { assertEquals(friendships.size.toLong(), result.totalElements) },
            { assertEquals(friendships.size, result.content.size) },
            { assertTrue(result.content.all { it.status == FriendshipStatus.PENDING }) },
            {
                assertTrue(result.content.all { friendship ->
                    friendships.any { it.requireId() == friendship.friendshipId }
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

    @Test
    fun `isSent - success - returns true when member sent friend request`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "sent-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "sent-receiver@test.com",
            nickname = "receiver"
        )

        // 친구 요청 보내기
        friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

        val result = friendshipReader.isSent(sender.requireId(), receiver.requireId())

        assertTrue(result)
    }

    @Test
    fun `isSent - success - returns false when no friend request was sent`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "nosent1@test.com",
            nickname = "nosent1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "nosent2@test.com",
            nickname = "nosent2"
        )

        val result = friendshipReader.isSent(member1.requireId(), member2.requireId())

        assertFalse(result)
    }

    @Test
    fun `isSent - success - returns false when member received friend request`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "reverse-sender@test.com",
            nickname = "sender"
        )
        val receiver = testMemberHelper.createActivatedMember(
            email = "reverse-receiver@test.com",
            nickname = "receiver"
        )

        // sender가 receiver에게 요청 보냄
        friendRequestor.sendFriendRequest(sender.requireId(), receiver.requireId())

        // receiver가 sender에게 요청을 보냈는지 확인 (false여야 함)
        val result = friendshipReader.isSent(receiver.requireId(), sender.requireId())

        assertFalse(result)
    }

    @Test
    fun `isSent - success - returns true when friendship is accepted`() {
        val member1 = testMemberHelper.createActivatedMember(
            email = "accepted-sent1@test.com",
            nickname = "accepted1"
        )
        val member2 = testMemberHelper.createActivatedMember(
            email = "accepted-sent2@test.com",
            nickname = "accepted2"
        )

        // 친구 관계 생성 및 수락
        createAcceptedFriendship(
            member1.requireId(),
            member1.nickname.value,
            member2.requireId(),
            member2.nickname.value
        )

        val result = friendshipReader.isSent(member1.requireId(), member2.requireId())

        assertTrue(result)
    }

    @Test
    fun `findFriendsByMemberId - success - excludes friendships with deactivated members`() {
        val activeMember = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )
        val deactivatedMember = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )
        val anotherActiveMember = testMemberHelper.createActivatedMember(
            email = "another@test.com",
            nickname = "another"
        )

        // 친구 관계 생성
        createAcceptedFriendship(
            activeMember.requireId(),
            activeMember.nickname.value,
            deactivatedMember.requireId(),
            deactivatedMember.nickname.value
        )
        createAcceptedFriendship(
            activeMember.requireId(),
            activeMember.nickname.value,
            anotherActiveMember.requireId(),
            anotherActiveMember.nickname.value
        )
        deactivatedMember.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findFriendsByMemberId(activeMember.requireId(), pageable)

        // 비활성화된 멤버와의 친구 관계는 제외되어야 함
        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(1, result.content.size) },
            { assertEquals(anotherActiveMember.requireId(), result.content.first().friendMemberId) },
            { assertFalse(result.content.any { it.friendMemberId == deactivatedMember.requireId() }) }
        )
    }

    @Test
    fun `findPendingRequestsReceived - success - excludes requests from deactivated members`() {
        val receiver = testMemberHelper.createActivatedMember(
            email = "receiver@test.com",
            nickname = "receiver"
        )
        val deactivatedSender = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )
        val activeSender = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )

        // 친구 요청 생성
        friendRequestor.sendFriendRequest(deactivatedSender.requireId(), receiver.requireId())
        friendRequestor.sendFriendRequest(activeSender.requireId(), receiver.requireId())
        deactivatedSender.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findPendingRequestsReceived(receiver.requireId(), pageable)

        // 비활성화된 멤버로부터의 요청은 제외되어야 함
        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(1, result.content.size) },
            { assertEquals(activeSender.requireId(), result.content.first().memberId) },
            { assertFalse(result.content.any { it.memberId == deactivatedSender.requireId() }) }
        )
    }

    @Test
    fun `findPendingRequestsSent - success - excludes requests to deactivated members`() {
        val sender = testMemberHelper.createActivatedMember(
            email = "sender@test.com",
            nickname = "sender"
        )
        val deactivatedReceiver = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )
        val activeReceiver = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )

        // 친구 요청 생성
        friendRequestor.sendFriendRequest(sender.requireId(), deactivatedReceiver.requireId())
        friendRequestor.sendFriendRequest(sender.requireId(), activeReceiver.requireId())
        deactivatedReceiver.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findPendingRequestsSent(sender.requireId(), pageable)

        // 비활성화된 멤버에게 보낸 요청은 제외되어야 함
        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(1, result.content.size) },
            { assertEquals(activeReceiver.requireId(), result.content.first().friendMemberId) },
            { assertFalse(result.content.any { it.friendMemberId == deactivatedReceiver.requireId() }) }
        )
    }

    @Test
    fun `searchFriends - success - excludes deactivated friends from search results`() {
        val member = testMemberHelper.createActivatedMember(
            email = "searcher@test.com",
            nickname = "searcher"
        )
        val deactivatedFriend = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivatedfriend"
        )
        val activeFriend = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "activefriend"
        )

        // 친구 관계 생성
        createAcceptedFriendship(
            member.requireId(),
            member.nickname.value,
            deactivatedFriend.requireId(),
            deactivatedFriend.nickname.value
        )
        createAcceptedFriendship(
            member.requireId(),
            member.nickname.value,
            activeFriend.requireId(),
            activeFriend.nickname.value
        )
        deactivatedFriend.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.searchFriends(member.requireId(), "active", pageable)

        // 비활성화된 친구는 검색 결과에서 제외되어야 함
        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(1, result.content.size) },
            { assertEquals(activeFriend.requireId(), result.content.first().friendMemberId) },
            { assertFalse(result.content.any { it.friendMemberId == deactivatedFriend.requireId() }) }
        )
    }

    @Test
    fun `findRecentFriends - success - excludes deactivated friends from recent friends`() {
        val member = testMemberHelper.createActivatedMember(
            email = "recent@test.com",
            nickname = "recent"
        )
        val deactivatedFriend = testMemberHelper.createActivatedMember(
            email = "deactivated@test.com",
            nickname = "deactivated"
        )
        val activeFriend = testMemberHelper.createActivatedMember(
            email = "active@test.com",
            nickname = "active"
        )

        // 친구 관계 생성
        createAcceptedFriendship(
            member.requireId(),
            member.nickname.value,
            deactivatedFriend.requireId(),
            deactivatedFriend.nickname.value
        )
        createAcceptedFriendship(
            member.requireId(),
            member.nickname.value,
            activeFriend.requireId(),
            activeFriend.nickname.value
        )
        deactivatedFriend.deactivate()
        flushAndClear()

        val pageable = PageRequest.of(0, 10)
        val result = friendshipReader.findRecentFriends(member.requireId(), pageable)

        // 비활성화된 친구는 최근 친구 목록에서 제외되어야 함
        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(1, result.content.size) },
            { assertEquals(activeFriend.requireId(), result.content.first().friendMemberId) },
            { assertFalse(result.content.any { it.friendMemberId == deactivatedFriend.requireId() }) }
        )
    }
}
