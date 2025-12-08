package com.albert.realmoneyrealtaste.domain.friend.event

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendRequestAcceptedEventTest {

    @Test
    fun `construct - success - creates FriendRequestAcceptedEvent with valid parameters`() {
        val friendshipId = 1L
        val fromMemberId = 100L
        val toMemberId = 200L

        val event = FriendRequestAcceptedEvent(
            friendshipId = friendshipId,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            occurredAt = LocalDateTime.now(),
        )

        assertEquals(friendshipId, event.friendshipId)
        assertEquals(fromMemberId, event.fromMemberId)
        assertEquals(toMemberId, event.toMemberId)
        assertTrue(event.occurredAt.isBefore(LocalDateTime.now()))
    }
}
