package com.albert.realmoneyrealtaste.domain.post.event

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class PostViewedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 99L,
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.viewerMemberId)
        assertEquals(99L, event.authorMemberId)
    }
}
