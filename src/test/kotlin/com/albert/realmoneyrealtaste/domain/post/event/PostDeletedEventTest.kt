package com.albert.realmoneyrealtaste.domain.post.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PostDeletedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
            occurredAt = LocalDateTime.now(),
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.authorMemberId)
        assertTrue(event.occurredAt.isBefore(LocalDateTime.now()))
    }

    @Test
    fun `withPostId - success - returns new event with updated postId`() {
        val originalPostId = 1L
        val newPostId = 999L
        val authorMemberId = 42L

        val originalEvent = PostDeletedEvent(
            postId = originalPostId,
            authorMemberId = authorMemberId,
            occurredAt = LocalDateTime.now(),
        )

        val updatedEvent = originalEvent.withPostId(newPostId)

        assertAll(
            { assertEquals(newPostId, updatedEvent.postId) },
            { assertEquals(authorMemberId, updatedEvent.authorMemberId) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withPostId - success - preserves immutability of original event`() {
        val originalEvent = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
            occurredAt = LocalDateTime.now(),
        )

        originalEvent.withPostId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.postId)
    }

    @Test
    fun `construction - success - handles edge case values`() {
        val event = PostDeletedEvent(
            postId = Long.MAX_VALUE,
            authorMemberId = Long.MAX_VALUE,
            occurredAt = LocalDateTime.now(),
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.postId) },
            { assertEquals(Long.MAX_VALUE, event.authorMemberId) }
        )
    }

    @Test
    fun `construction - success - handles minimum values`() {
        val event = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 1L,
            occurredAt = LocalDateTime.now(),
        )

        assertAll(
            { assertEquals(1L, event.postId) },
            { assertEquals(1L, event.authorMemberId) }
        )
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val occurredAt = LocalDateTime.now()
        val event1 = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
            occurredAt,
        )

        val event2 = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
            occurredAt,
        )

        val event3 = PostDeletedEvent(
            postId = 2L,
            authorMemberId = 42L,
            occurredAt,
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
        val event = PostDeletedEvent(
            postId = 123L,
            authorMemberId = 456L,
            occurredAt = LocalDateTime.now(),
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("123")) },
            { assertTrue(toString.contains("456")) }
        )
    }

    @Test
    fun `equals - success - different authorMemberId produces different event`() {
        val event1 = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 42L,
            occurredAt = LocalDateTime.now(),
        )

        val event2 = PostDeletedEvent(
            postId = 1L,
            authorMemberId = 43L,
            occurredAt = LocalDateTime.now(),
        )

        assertNotEquals(event1, event2)
    }
}
