package com.albert.realmoneyrealtaste.domain.friend.event

import kotlin.test.Test
import kotlin.test.assertEquals

class FriendRequestRejectedEventTest {

    @Test
    fun `construct - success - creates FriendRequestRejectedEvent with valid parameters`() {
        val friendshipId = 1L
        val fromMemberId = 100L
        val toMemberId = 200L

        val event = FriendRequestRejectedEvent(
            friendshipId = friendshipId,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId
        )

        assertEquals(friendshipId, event.friendshipId)
        assertEquals(fromMemberId, event.fromMemberId)
        assertEquals(toMemberId, event.toMemberId)
    }
}
