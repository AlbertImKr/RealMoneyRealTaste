package com.albert.realmoneyrealtaste.domain.post.event

import kotlin.test.Test
import kotlin.test.assertEquals

class PostCreatedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place"
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.authorMemberId)
        assertEquals("Delicious Place", event.restaurantName)
    }
}
