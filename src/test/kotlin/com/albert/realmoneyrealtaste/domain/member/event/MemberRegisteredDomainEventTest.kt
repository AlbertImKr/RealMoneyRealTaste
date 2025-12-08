package com.albert.realmoneyrealtaste.domain.member.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MemberRegisteredDomainEventTest {

    @Test
    fun `create - success - creates event with all properties`() {
        val memberId = 1L
        val email = "test@example.com"
        val nickname = "testUser"
        val occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val event = MemberRegisteredDomainEvent(
            memberId = memberId,
            email = email,
            nickname = nickname,
            occurredAt = occurredAt
        )

        assertAll(
            { assertEquals(memberId, event.memberId) },
            { assertEquals(email, event.email) },
            { assertEquals(nickname, event.nickname) },
            { assertEquals(occurredAt, event.occurredAt) }
        )
    }

    @Test
    fun `create - success - uses default occurredAt when not provided`() {
        val before = LocalDateTime.now()

        val event = MemberRegisteredDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            nickname = "testUser"
        )

        val after = LocalDateTime.now()

        assertTrue(event.occurredAt >= before)
        assertTrue(event.occurredAt <= after)
    }

    @Test
    fun `withMemberId - success - returns new event with updated memberId`() {
        val originalMemberId = 1L
        val newMemberId = 999L
        val email = "test@example.com"
        val nickname = "testUser"
        val occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val originalEvent = MemberRegisteredDomainEvent(
            memberId = originalMemberId,
            email = email,
            nickname = nickname,
            occurredAt = occurredAt
        )

        val updatedEvent = originalEvent.withMemberId(newMemberId)

        assertAll(
            { assertEquals(newMemberId, updatedEvent.memberId) },
            { assertEquals(email, updatedEvent.email) },
            { assertEquals(nickname, updatedEvent.nickname) },
            { assertEquals(occurredAt, updatedEvent.occurredAt) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withMemberId - success - preserves immutability of original event`() {
        val originalEvent = MemberRegisteredDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            nickname = "testUser"
        )

        originalEvent.withMemberId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.memberId)
    }

    @Test
    fun `create - success - handles edge case values`() {
        val event = MemberRegisteredDomainEvent(
            memberId = Long.MAX_VALUE,
            email = "a@b.c",
            nickname = "a",
            occurredAt = LocalDateTime.MAX
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.memberId) },
            { assertEquals("a@b.c", event.email) },
            { assertEquals("a", event.nickname) },
            { assertEquals(LocalDateTime.MAX, event.occurredAt) }
        )
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val event1 = MemberRegisteredDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            nickname = "testUser",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val event2 = MemberRegisteredDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            nickname = "testUser",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val event3 = MemberRegisteredDomainEvent(
            memberId = 2L,
            email = "test@example.com",
            nickname = "testUser",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
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
        val event = MemberRegisteredDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            nickname = "testUser",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("1")) },
            { assertTrue(toString.contains("test@example.com")) },
            { assertTrue(toString.contains("testUser")) },
            { assertTrue(toString.contains("2023-01-01T12:00")) }
        )
    }
}
