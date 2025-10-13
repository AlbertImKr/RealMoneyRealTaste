package com.albert.realmoneyrealtaste.domain.post.event

import kotlin.test.Test

class PostDeletedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
        )

        assert(event.postId == 1L)
        assert(event.authorMemberId == 42L)
    }
}
