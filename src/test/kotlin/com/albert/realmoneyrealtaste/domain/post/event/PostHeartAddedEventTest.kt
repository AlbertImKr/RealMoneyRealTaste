package com.albert.realmoneyrealtaste.domain.post.event

import kotlin.test.Test

class PostHeartAddedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostHeartAddedEvent(
            postId = 1L,
            memberId = 42L,
        )

        assert(event.postId == 1L)
        assert(event.memberId == 42L)
    }
}
