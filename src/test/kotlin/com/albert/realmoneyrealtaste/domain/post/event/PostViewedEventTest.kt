package com.albert.realmoneyrealtaste.domain.post.event

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PostViewedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.viewerMemberId)
        assertEquals(100L, event.authorMemberId)
    }

    @Test
    fun `withPostId - success - returns new event with updated postId`() {
        val originalPostId = 1L
        val newPostId = 999L
        val viewerMemberId = 42L
        val authorMemberId = 100L

        val originalEvent = PostViewedEvent(
            postId = originalPostId,
            viewerMemberId = viewerMemberId,
            authorMemberId = authorMemberId
        )

        val updatedEvent = originalEvent.withPostId(newPostId)

        assertAll(
            { assertEquals(newPostId, updatedEvent.postId) },
            { assertEquals(viewerMemberId, updatedEvent.viewerMemberId) },
            { assertEquals(authorMemberId, updatedEvent.authorMemberId) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withPostId - success - preserves immutability of original event`() {
        val originalEvent = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        originalEvent.withPostId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.postId)
    }

    @Test
    fun `construction - success - handles edge case values`() {
        val event = PostViewedEvent(
            postId = Long.MAX_VALUE,
            viewerMemberId = Long.MAX_VALUE,
            authorMemberId = Long.MAX_VALUE
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.postId) },
            { assertEquals(Long.MAX_VALUE, event.viewerMemberId) },
            { assertEquals(Long.MAX_VALUE, event.authorMemberId) }
        )
    }

    @Test
    fun `construction - success - handles minimum values`() {
        val event = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 1L,
            authorMemberId = 1L
        )

        assertAll(
            { assertEquals(1L, event.postId) },
            { assertEquals(1L, event.viewerMemberId) },
            { assertEquals(1L, event.authorMemberId) }
        )
    }

    @Test
    fun `construction - success - viewer and author can be same member`() {
        val memberId = 42L

        val event = PostViewedEvent(
            postId = 1L,
            viewerMemberId = memberId,
            authorMemberId = memberId
        )

        assertAll(
            { assertEquals(1L, event.postId) },
            { assertEquals(memberId, event.viewerMemberId) },
            { assertEquals(memberId, event.authorMemberId) }
        )
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val event1 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        val event2 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        val event3 = PostViewedEvent(
            postId = 2L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        assertAll(
            { assertEquals(event1, event2) },
            { assertEquals(event1.hashCode(), event2.hashCode()) },
            { assertNotEquals(event1, event3) },
            { assertNotEquals(event1.hashCode(), event3.hashCode()) }
        )
    }

    @Test
    fun `toString - success - contains all properties`() {
        val event = PostViewedEvent(
            postId = 123L,
            viewerMemberId = 456L,
            authorMemberId = 789L
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("123")) },
            { assertTrue(toString.contains("456")) },
            { assertTrue(toString.contains("789")) }
        )
    }

    @Test
    fun `equals - success - different viewerMemberId produces different event`() {
        val event1 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        val event2 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 43L,
            authorMemberId = 100L
        )

        assertNotEquals(event1, event2)
    }

    @Test
    fun `equals - success - different authorMemberId produces different event`() {
        val event1 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 100L
        )

        val event2 = PostViewedEvent(
            postId = 1L,
            viewerMemberId = 42L,
            authorMemberId = 101L
        )

        assertNotEquals(event1, event2)
    }
}
