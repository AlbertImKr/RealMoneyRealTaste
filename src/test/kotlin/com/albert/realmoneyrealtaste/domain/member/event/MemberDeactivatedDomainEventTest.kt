package com.albert.realmoneyrealtaste.domain.member.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MemberDeactivatedDomainEventTest {

    @Test
    fun `create - success - creates event with memberId`() {
        val memberId = 1L

        val event = MemberDeactivatedDomainEvent(
            memberId = memberId
        )

        assertEquals(memberId, event.memberId)
    }

    @Test
    fun `withMemberId - success - returns new event with updated memberId`() {
        val originalMemberId = 1L
        val newMemberId = 999L

        val originalEvent = MemberDeactivatedDomainEvent(
            memberId = originalMemberId
        )

        val updatedEvent = originalEvent.withMemberId(newMemberId)

        assertAll(
            { assertEquals(newMemberId, updatedEvent.memberId) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withMemberId - success - preserves immutability of original event`() {
        val originalEvent = MemberDeactivatedDomainEvent(
            memberId = 1L
        )

        originalEvent.withMemberId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.memberId)
    }

    @Test
    fun `create - success - handles edge case values`() {
        val event = MemberDeactivatedDomainEvent(
            memberId = Long.MAX_VALUE
        )

        assertEquals(Long.MAX_VALUE, event.memberId)
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val occurredAt = LocalDateTime.now()
        val event1 = MemberDeactivatedDomainEvent(memberId = 1L, occurredAt)
        val event2 = MemberDeactivatedDomainEvent(memberId = 1L, occurredAt)
        val event3 = MemberDeactivatedDomainEvent(memberId = 2L, occurredAt)

        assertAll(
            { assertEquals(event1, event2) },
            { assertEquals(event1.hashCode(), event2.hashCode()) },
            { assertNotEquals(event1, event3) },
            { assertNotEquals(event1.hashCode(), event3.hashCode()) }
        )
    }

    @Test
    fun `toString - success - contains memberId`() {
        val event = MemberDeactivatedDomainEvent(memberId = 123L)

        val toString = event.toString()

        assertTrue(toString.contains("123"))
    }

    @Test
    fun `create - success - handles minimum memberId`() {
        val event = MemberDeactivatedDomainEvent(memberId = 1L)

        assertEquals(1L, event.memberId)
    }
}
