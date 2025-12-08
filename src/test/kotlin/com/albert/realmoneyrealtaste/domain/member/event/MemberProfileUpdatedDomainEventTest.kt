package com.albert.realmoneyrealtaste.domain.member.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberProfileUpdatedDomainEventTest {

    @Test
    fun `create - success - creates event with all properties`() {
        val memberId = 1L
        val email = "test@example.com"
        val updatedFields = listOf("nickname", "imageId")
        val nickname = "newNickname"
        val imageId = 123L
        val occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = email,
            updatedFields = updatedFields,
            nickname = nickname,
            imageId = imageId,
            occurredAt = occurredAt
        )

        assertAll(
            { assertEquals(memberId, event.memberId) },
            { assertEquals(email, event.email) },
            { assertEquals(updatedFields, event.updatedFields) },
            { assertEquals(nickname, event.nickname) },
            { assertEquals(imageId, event.imageId) },
            { assertEquals(occurredAt, event.occurredAt) }
        )
    }

    @Test
    fun `create - success - uses default occurredAt when not provided`() {
        val before = LocalDateTime.now()

        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname")
        )

        val after = LocalDateTime.now()

        assertTrue(event.occurredAt >= before)
        assertTrue(event.occurredAt <= after)
    }

    @Test
    fun `create - success - with only nickname updated`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "newNickname"
        )

        assertAll(
            { assertEquals(1L, event.memberId) },
            { assertEquals("test@example.com", event.email) },
            { assertEquals(listOf("nickname"), event.updatedFields) },
            { assertEquals("newNickname", event.nickname) },
            { assertNull(event.imageId) }
        )
    }

    @Test
    fun `create - success - with only imageId updated`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("imageId"),
            imageId = 123L
        )

        assertAll(
            { assertEquals(1L, event.memberId) },
            { assertEquals("test@example.com", event.email) },
            { assertEquals(listOf("imageId"), event.updatedFields) },
            { assertNull(event.nickname) },
            { assertEquals(123L, event.imageId) }
        )
    }

    @Test
    fun `create - success - with both nickname and imageId updated`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname", "imageId"),
            nickname = "newNickname",
            imageId = 123L
        )

        assertAll(
            { assertEquals(1L, event.memberId) },
            { assertEquals("test@example.com", event.email) },
            { assertEquals(listOf("nickname", "imageId"), event.updatedFields) },
            { assertEquals("newNickname", event.nickname) },
            { assertEquals(123L, event.imageId) }
        )
    }

    @Test
    fun `withMemberId - success - returns new event with updated memberId`() {
        val originalMemberId = 1L
        val newMemberId = 999L
        val email = "test@example.com"
        val updatedFields = listOf("nickname")
        val nickname = "newNickname"

        val originalEvent = MemberProfileUpdatedDomainEvent(
            memberId = originalMemberId,
            email = email,
            updatedFields = updatedFields,
            nickname = nickname
        )

        val updatedEvent = originalEvent.withMemberId(newMemberId)

        assertAll(
            { assertEquals(newMemberId, updatedEvent.memberId) },
            { assertEquals(email, updatedEvent.email) },
            { assertEquals(updatedFields, updatedEvent.updatedFields) },
            { assertEquals(nickname, updatedEvent.nickname) },
            { assertNull(updatedEvent.imageId) },
            { assertNotEquals(originalEvent, updatedEvent) }
        )
    }

    @Test
    fun `withMemberId - success - preserves immutability of original event`() {
        val originalEvent = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "newNickname"
        )

        originalEvent.withMemberId(999L)

        // 원본 이벤트는 변경되지 않아야 함
        assertEquals(1L, originalEvent.memberId)
    }

    @Test
    fun `create - success - handles edge case values`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = Long.MAX_VALUE,
            email = "a@b.c",
            updatedFields = listOf("a"),
            nickname = "",
            imageId = 0L,
            occurredAt = LocalDateTime.MAX
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.memberId) },
            { assertEquals("a@b.c", event.email) },
            { assertEquals(listOf("a"), event.updatedFields) },
            { assertEquals("", event.nickname) },
            { assertEquals(0L, event.imageId) },
            { assertEquals(LocalDateTime.MAX, event.occurredAt) }
        )
    }

    @Test
    fun `equals and hashCode - success - works correctly`() {
        val event1 = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "newNickname",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val event2 = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "newNickname",
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val event3 = MemberProfileUpdatedDomainEvent(
            memberId = 2L,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "newNickname",
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
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = listOf("nickname", "imageId"),
            nickname = "newNickname",
            imageId = 123L,
            occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val toString = event.toString()

        assertAll(
            { assertTrue(toString.contains("1")) },
            { assertTrue(toString.contains("test@example.com")) },
            { assertTrue(toString.contains("[nickname, imageId]")) },
            { assertTrue(toString.contains("newNickname")) },
            { assertTrue(toString.contains("123")) },
            { assertTrue(toString.contains("2023-01-01T12:00")) }
        )
    }

    @Test
    fun `create - success - handles Korean characters`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "한글@example.com",
            updatedFields = listOf("닉네임"),
            nickname = "홍길동"
        )

        assertAll(
            { assertEquals(1L, event.memberId) },
            { assertEquals("한글@example.com", event.email) },
            { assertEquals(listOf("닉네임"), event.updatedFields) },
            { assertEquals("홍길동", event.nickname) }
        )
    }

    @Test
    fun `create - success - with empty updatedFields list`() {
        val event = MemberProfileUpdatedDomainEvent(
            memberId = 1L,
            email = "test@example.com",
            updatedFields = emptyList()
        )

        assertTrue(event.updatedFields.isEmpty())
    }
}
