package com.albert.realmoneyrealtaste.domain.friend.event

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendshipTerminatedEventTest {

    @Test
    fun `construct - success - creates FriendshipTerminatedEvent with valid parameters`() {
        val memberId = 100L
        val friendMemberId = 200L

        val event = FriendshipTerminatedEvent(
            memberId = memberId,
            friendMemberId = friendMemberId,
            friendshipId = 1L,
            occurredAt = LocalDateTime.now(),
        )

        assertEquals(memberId, event.memberId)
        assertEquals(friendMemberId, event.friendMemberId)
        assertEquals(1L, event.friendshipId)
        assertTrue(event.occurredAt.isBefore(LocalDateTime.now()))
    }
}
