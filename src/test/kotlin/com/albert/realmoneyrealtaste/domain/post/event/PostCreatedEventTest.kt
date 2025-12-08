package com.albert.realmoneyrealtaste.domain.post.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PostCreatedEventTest {

    @Test
    fun `construction - success - creates event with valid parameters`() {
        val event = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place",
            occurredAt = LocalDateTime.now(),
        )

        assertEquals(1L, event.postId)
        assertEquals(42L, event.authorMemberId)
        assertEquals("Delicious Place", event.restaurantName)
        assertTrue(event.occurredAt.isBefore(LocalDateTime.now()))
    }

    @Test
    fun `withPostId - success - returns new event with updated postId`() {
        val originalPostId = 1L
        val newPostId = 999L
        val authorMemberId = 42L
        val restaurantName = "Delicious Place"

        val originalEvent = PostCreatedEvent(
            postId = originalPostId,
            authorMemberId = authorMemberId,
            restaurantName = restaurantName,
            occurredAt = LocalDateTime.now(),
        )

        val updatedEvent = originalEvent.withPostId(newPostId)

        assertAll(
            { assertEquals(newPostId, updatedEvent.postId) },
            { assertEquals(authorMemberId, updatedEvent.authorMemberId) },
            { assertEquals(restaurantName, updatedEvent.restaurantName) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withPostId - success - preserves immutability of original event`() {
        val originalEvent = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place",
            occurredAt = LocalDateTime.now(),
        )

        originalEvent.withPostId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.postId)
    }

    @Test
    fun `construction - success - handles edge case values`() {
        val event = PostCreatedEvent(
            postId = Long.MAX_VALUE,
            authorMemberId = Long.MAX_VALUE,
            restaurantName = "",
            occurredAt = LocalDateTime.now(),
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.postId) },
            { assertEquals(Long.MAX_VALUE, event.authorMemberId) },
            { assertEquals("", event.restaurantName) }
        )
    }

    @Test
    fun `construction - success - handles Korean characters`() {
        val event = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "맛있는 식당",
            occurredAt = LocalDateTime.now(),
        )

        assertEquals("맛있는 식당", event.restaurantName)
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val occurredAt = LocalDateTime.now()
        val event1 = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place",
            occurredAt = occurredAt
        )

        val event2 = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place",
            occurredAt = occurredAt
        )

        val event3 = PostCreatedEvent(
            postId = 2L,
            authorMemberId = 42L,
            restaurantName = "Delicious Place",
            occurredAt = occurredAt
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
        val event = PostCreatedEvent(
            postId = 123L,
            authorMemberId = 456L,
            restaurantName = "Test Restaurant",
            occurredAt = LocalDateTime.now(),
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("123")) },
            { assertTrue(toString.contains("456")) },
            { assertTrue(toString.contains("Test Restaurant")) }
        )
    }

    @Test
    fun `construction - success - handles special characters in restaurant name`() {
        val event = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 42L,
            restaurantName = "Special & Unique's Place! @#$%",
            occurredAt = LocalDateTime.now(),
        )

        assertEquals("Special & Unique's Place! @#$%", event.restaurantName)
    }

    @Test
    fun `construction - success - handles minimum postId`() {
        val event = PostCreatedEvent(
            postId = 1L,
            authorMemberId = 1L,
            restaurantName = "A",
            occurredAt = LocalDateTime.now(),
        )

        assertAll(
            { assertEquals(1L, event.postId) },
            { assertEquals(1L, event.authorMemberId) },
            { assertEquals("A", event.restaurantName) }
        )
    }
}
