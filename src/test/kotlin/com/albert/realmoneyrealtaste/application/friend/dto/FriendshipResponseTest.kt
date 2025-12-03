package com.albert.realmoneyrealtaste.application.friend.dto

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import org.junit.jupiter.api.Assertions.assertAll
import java.lang.reflect.Field
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FriendshipResponseTest {

    @Test
    fun `from - success - creates response with all friendship information`() {
        val fromMemberId = 1L
        val memberNickname = "sender"
        val toMemberId = 2L
        val friendNickname = "receiver"
        val command = FriendRequestCommand(fromMemberId, memberNickname, toMemberId, friendNickname)
        val friendship = Friendship.request(command)

        // ID 설정 (실제로는 JPA가 수행)
        setFriendshipId(friendship, 100L)

        val result = FriendshipResponse.from(friendship, friendNickname)

        assertAll(
            { assertEquals(100L, result.friendshipId) },
            { assertEquals(fromMemberId, result.memberId) },
            { assertEquals(toMemberId, result.friendMemberId) },
            { assertEquals(friendNickname, result.friendNickname) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertEquals(friendship.createdAt, result.createdAt) },
            { assertEquals(friendship.updatedAt, result.updatedAt) },
            { assertEquals(toMemberId, result.id) },
            { assertEquals(friendNickname, result.nickname) },
            { assertEquals(0, result.mutualFriendsCount) },
            { assertEquals(friendship.createdAt, result.friendSince) },
            { assertNull(result.profileImageUrl) }
        )
    }

    @Test
    fun `constructor - success - initializes all fields correctly`() {
        val friendshipId = 200L
        val memberId = 3L
        val friendMemberId = 4L
        val friendNickname = "bob"
        val status = FriendshipStatus.ACCEPTED
        val createdAt = friendshipId.let { java.time.LocalDateTime.now().minusDays(1) }
        val updatedAt = friendshipId.let { java.time.LocalDateTime.now() }

        val response = FriendshipResponse(
            friendshipId,
            memberId,
            friendMemberId,
            friendNickname,
            status,
            createdAt,
            updatedAt
        )

        assertAll(
            { assertEquals(friendshipId, response.friendshipId) },
            { assertEquals(memberId, response.memberId) },
            { assertEquals(friendMemberId, response.friendMemberId) },
            { assertEquals(friendNickname, response.friendNickname) },
            { assertEquals(status, response.status) },
            { assertEquals(createdAt, response.createdAt) },
            { assertEquals(updatedAt, response.updatedAt) },
            { assertEquals(friendMemberId, response.id) },
            { assertEquals(friendNickname, response.nickname) },
            { assertEquals(0, response.mutualFriendsCount) },
            { assertEquals(createdAt, response.friendSince) },
            { assertNull(response.profileImageUrl) }
        )
    }

    @Test
    fun `from - success - includes optional parameters correctly`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val memberNickname = "sender"
        val friendNickname = "receiver"
        val command = FriendRequestCommand(fromMemberId, memberNickname, toMemberId, friendNickname)
        val friendship = Friendship.request(command)
        setFriendshipId(friendship, 100L)

        val mutualFriendsCount = 5
        val profileImageUrl = "https://example.com/profile.jpg"

        val result = FriendshipResponse.from(
            friendship,
            friendNickname,
            mutualFriendsCount,
            profileImageUrl
        )

        assertAll(
            { assertEquals(mutualFriendsCount, result.mutualFriendsCount) },
            { assertEquals(profileImageUrl, result.profileImageUrl) }
        )
    }

    @Test
    fun `from - success - works with different friendship statuses`() {
        val fromMemberId = 1L
        val memberNickname = "sender"
        val toMemberId = 2L
        val friendNickname = "receiver"

        // 각 상태별로 테스트
        FriendshipStatus.values().forEach { status ->
            val command = FriendRequestCommand(fromMemberId, memberNickname, toMemberId, friendNickname)
            val friendship = Friendship.request(command)
            setFriendshipId(friendship, 100L)

            // 상태 강제 설정
            setStatus(friendship, status)

            val result = FriendshipResponse.from(friendship, friendNickname)

            assertEquals(status, result.status, "Status should be $status")
        }
    }

    @Test
    fun `constructor - success - custom template fields override defaults`() {
        val friendshipId = 200L
        val memberId = 3L
        val friendMemberId = 4L
        val friendNickname = "bob"
        val status = FriendshipStatus.ACCEPTED
        val createdAt = java.time.LocalDateTime.now().minusDays(1)
        val updatedAt = java.time.LocalDateTime.now()

        val customId = 999L
        val customNickname = "custom_nickname"
        val customMutualFriendsCount = 10
        val customFriendSince = java.time.LocalDateTime.now().minusDays(2)
        val customProfileImageUrl = "https://custom.example.com/image.jpg"

        val response = FriendshipResponse(
            friendshipId = friendshipId,
            memberId = memberId,
            friendMemberId = friendMemberId,
            friendNickname = friendNickname,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            id = customId,
            nickname = customNickname,
            mutualFriendsCount = customMutualFriendsCount,
            friendSince = customFriendSince,
            profileImageUrl = customProfileImageUrl
        )

        assertAll(
            { assertEquals(customId, response.id) },
            { assertEquals(customNickname, response.nickname) },
            { assertEquals(customMutualFriendsCount, response.mutualFriendsCount) },
            { assertEquals(customFriendSince, response.friendSince) },
            { assertEquals(customProfileImageUrl, response.profileImageUrl) }
        )
    }

    private fun setFriendshipId(friendship: Friendship, id: Long) {
        val field: Field = BaseEntity::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(friendship, id)
    }

    private fun setStatus(friendship: Friendship, status: FriendshipStatus) {
        val field: Field = Friendship::class.java.getDeclaredField("status")
        field.isAccessible = true
        field.set(friendship, status)
    }
}
