package com.albert.realmoneyrealtaste.domain.post

import org.junit.jupiter.api.Assertions.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostHeartTest {

    @Test
    fun `create - success - creates PostHeart with valid parameters`() {
        val postId = 1L
        val memberId = 2L
        val beforeCreatAt = LocalDateTime.now()

        val postHeart = PostHeart.create(postId, memberId)

        assertAll(
            { assertEquals(postId, postHeart.postId) },
            { assertEquals(memberId, postHeart.memberId) },
            { assertTrue(postHeart.createdAt >= beforeCreatAt) }
        )
    }

    @Test
    fun `setters - success - for coverage frameworks`() {
        val testPostHeart = TestPostHeart()
        testPostHeart.setPostIdForTest(1L)
        testPostHeart.setMemberIdForTest(2L)
        val now = LocalDateTime.now()
        testPostHeart.setCreatedAtForTest(now)

        assertAll(
            { assertEquals(1L, testPostHeart.postId) },
            { assertEquals(2L, testPostHeart.memberId) },
            { assertEquals(now, testPostHeart.createdAt) }
        )
    }

    private class TestPostHeart : PostHeart(1, 2, LocalDateTime.now()) {
        fun setPostIdForTest(postId: Long) {
            this.postId = postId
        }

        fun setMemberIdForTest(memberId: Long) {
            this.memberId = memberId
        }

        fun setCreatedAtForTest(createdAt: LocalDateTime) {
            this.createdAt = createdAt
        }
    }
}
