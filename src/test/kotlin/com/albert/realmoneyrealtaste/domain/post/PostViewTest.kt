package com.albert.realmoneyrealtaste.domain.post

import org.junit.jupiter.api.Assertions.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostViewTest {

    @Test
    fun `create - success - creates PostView with valid parameters`() {
        val postId = 1L
        val memberId = 2L
        val beforeViewAt = LocalDateTime.now()

        val postView = PostView.create(postId, memberId)

        assertAll(
            { assertEquals(postId, postView.postId) },
            { assertEquals(memberId, postView.memberId) },
            { assertTrue(postView.viewAt >= beforeViewAt) }
        )
    }

    @Test
    fun `setters - success - for coverage frameworks`() {
        val testPostView = TestPostView()
        testPostView.setPostIdForTest(1L)
        testPostView.setMemberIdForTest(2L)
        val now = LocalDateTime.now()
        testPostView.setViewAtForTest(now)

        assertAll(
            { assertEquals(1L, testPostView.postId) },
            { assertEquals(2L, testPostView.memberId) },
            { assertEquals(now, testPostView.viewAt) }
        )
    }

    private class TestPostView : PostView(1, 2, LocalDateTime.now()) {
        fun setPostIdForTest(postId: Long) {
            this.postId = postId
        }

        fun setMemberIdForTest(memberId: Long) {
            this.memberId = memberId
        }

        fun setViewAtForTest(viewAt: LocalDateTime) {
            this.viewAt = viewAt
        }
    }
}
