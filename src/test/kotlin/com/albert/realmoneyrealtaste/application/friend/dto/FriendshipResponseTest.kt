package com.albert.realmoneyrealtaste.application.friend.dto

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import org.junit.jupiter.api.Assertions.assertAll
import java.lang.reflect.Field
import kotlin.test.Test
import kotlin.test.assertEquals

class FriendshipResponseTest {

    @Test
    fun `from - success - creates response with all friendship information`() {
        val fromMemberId = 1L
        val toMemberId = 2L
        val memberNickname = "sender"
        val friendNickname = "receiver"
        val command = FriendRequestCommand(fromMemberId, toMemberId)
        val friendship = Friendship.request(command)

        // ID 설정 (실제로는 JPA가 수행)
        setFriendshipId(friendship, 100L)

        val result = FriendshipResponse.from(friendship, memberNickname, friendNickname)

        assertAll(
            { assertEquals(100L, result.friendshipId) },
            { assertEquals(fromMemberId, result.memberId) },
            { assertEquals(memberNickname, result.memberNickname) },
            { assertEquals(toMemberId, result.friendMemberId) },
            { assertEquals(friendNickname, result.friendNickname) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertEquals(friendship.createdAt, result.createdAt) },
            { assertEquals(friendship.updatedAt, result.updatedAt) }
        )
    }

    @Test
    fun `constructor - success - initializes all fields correctly`() {
        val friendshipId = 200L
        val memberId = 3L
        val memberNickname = "alice"
        val friendMemberId = 4L
        val friendNickname = "bob"
        val status = FriendshipStatus.ACCEPTED
        val createdAt = friendshipId.let { java.time.LocalDateTime.now().minusDays(1) }
        val updatedAt = friendshipId.let { java.time.LocalDateTime.now() }

        val response = FriendshipResponse(
            friendshipId,
            memberId,
            memberNickname,
            friendMemberId,
            friendNickname,
            status,
            createdAt,
            updatedAt
        )

        assertAll(
            { assertEquals(friendshipId, response.friendshipId) },
            { assertEquals(memberId, response.memberId) },
            { assertEquals(memberNickname, response.memberNickname) },
            { assertEquals(friendMemberId, response.friendMemberId) },
            { assertEquals(friendNickname, response.friendNickname) },
            { assertEquals(status, response.status) },
            { assertEquals(createdAt, response.createdAt) },
            { assertEquals(updatedAt, response.updatedAt) }
        )
    }

    private fun setFriendshipId(friendship: Friendship, id: Long) {
        val field: Field = BaseEntity::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(friendship, id)
    }
}
