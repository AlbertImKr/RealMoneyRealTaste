package com.albert.realmoneyrealtaste.domain.post.event

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PostHeartRemovedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 42L
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.memberId)
    }

    @Test
    fun `withPostId - success - returns new event with updated postId`() {
        val originalPostId = 1L
        val newPostId = 999L
        val memberId = 42L

        val originalEvent = PostHeartRemovedEvent(
            postId = originalPostId,
            memberId = memberId
        )

        val updatedEvent = originalEvent.withPostId(newPostId)

        assertAll(
            { assertEquals(newPostId, updatedEvent.postId) },
            { assertEquals(memberId, updatedEvent.memberId) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withPostId - success - preserves immutability of original event`() {
        val originalEvent = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 42L
        )

        originalEvent.withPostId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.postId)
    }

    @Test
    fun `construction - success - handles edge case values`() {
        val event = PostHeartRemovedEvent(
            postId = Long.MAX_VALUE,
            memberId = Long.MAX_VALUE
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.postId) },
            { assertEquals(Long.MAX_VALUE, event.memberId) }
        )
    }

    @Test
    fun `construction - success - handles minimum values`() {
        val event = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 1L
        )

        assertAll(
            { assertEquals(1L, event.postId) },
            { assertEquals(1L, event.memberId) }
        )
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val event1 = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 42L
        )

        val event2 = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 42L
        )

        val event3 = PostHeartRemovedEvent(
            postId = 2L,
            memberId = 42L
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
        val event = PostHeartRemovedEvent(
            postId = 123L,
            memberId = 456L
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("123")) },
            { assertTrue(toString.contains("456")) }
        )
    }

    @Test
    fun `equals - success - different memberId produces different event`() {
        val event1 = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 42L
        )

        val event2 = PostHeartRemovedEvent(
            postId = 1L,
            memberId = 43L
        )

        assertNotEquals(event1, event2)
    }

    @Test
    fun `construction - success - postId and memberId can be different`() {
        val event = PostHeartRemovedEvent(
            postId = 100L,
            memberId = 200L
        )

        assertAll(
            { assertEquals(100L, event.postId) },
            { assertEquals(200L, event.memberId) }
        )
    }
}
