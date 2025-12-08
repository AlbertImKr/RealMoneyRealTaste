package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.event.required.MemberEventRepository
import com.albert.realmoneyrealtaste.domain.event.MemberEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberEventUpdaterTest(
    val memberEventCreator: MemberEventCreator,
    val memberEventUpdater: MemberEventUpdater,
    val memberEventRepository: MemberEventRepository,
) : IntegrationTestBase() {

    @Test
    fun `markAllAsRead - success - marks all unread events as read`() {
        val memberId = 1L

        // 여러 개의 안 읽은 이벤트 생성
        repeat(5) { index ->
            memberEventCreator.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "알림 $index",
                message = "메시지 $index"
            )
        }

        // 이미 읽은 이벤트 생성
        val readEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            title = "이미 읽은 알림",
            message = "이미 읽은 메시지"
        )
        memberEventUpdater.markAsRead(readEvent.requireId(), memberId)

        // 모든 이벤트를 읽음으로 표시
        val markedCount = memberEventUpdater.markAllAsRead(memberId)

        // 5개의 안 읽은 이벤트만 읽음으로 표시되어야 함
        assertEquals(5, markedCount)
    }

    @Test
    fun `markAllAsRead - success - returns 0 when no unread events exist`() {
        val memberId = 1L

        // 이미 모두 읽은 이벤트들 생성
        repeat(3) { index ->
            val event = memberEventCreator.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "알림 $index",
                message = "메시지 $index"
            )
            memberEventUpdater.markAsRead(event.requireId(), memberId)
        }

        // 모두 읽음으로 표시 시도
        val markedCount = memberEventUpdater.markAllAsRead(memberId)

        assertEquals(0, markedCount)
    }

    @Test
    fun `markAllAsRead - success - returns 0 when member has no events`() {
        val memberId = 999L

        val markedCount = memberEventUpdater.markAllAsRead(memberId)

        assertEquals(0, markedCount)
    }

    @Test
    fun `markAsRead - success - marks specific event as read`() {
        val memberId = 1L

        // 여러 이벤트 생성
        val event1 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "첫 번째 알림",
            message = "첫 번째 메시지"
        )
        val event2 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            title = "두 번째 알림",
            message = "두 번째 메시지"
        )

        // 특정 이벤트만 읽음으로 표시
        val updatedEvent = memberEventUpdater.markAsRead(event1.requireId(), memberId)

        assertNotNull(updatedEvent)
        assertEquals(event1.requireId(), updatedEvent.requireId())
        assertTrue(updatedEvent.isRead)

        // 다른 이벤트는 여전히 안 읽음 상태
        val retrievedEvent2 = memberEventUpdater.markAsRead(event2.requireId(), memberId)
        assertTrue(retrievedEvent2.isRead)
    }

    @Test
    fun `markAsRead - success - can mark already read event`() {
        val memberId = 1L

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "알림",
            message = "메시지"
        )

        // 첫 번째 읽음 표시
        val updatedEvent1 = memberEventUpdater.markAsRead(event.requireId(), memberId)
        assertTrue(updatedEvent1.isRead)

        // 두 번째 읽음 표시 (이미 읽은 상태)
        val updatedEvent2 = memberEventUpdater.markAsRead(event.requireId(), memberId)
        assertTrue(updatedEvent2.isRead)
        assertEquals(updatedEvent1.requireId(), updatedEvent2.requireId())
    }

    @Test
    fun `markAsRead - failure - throws exception when event does not exist`() {
        val memberId = 1L
        val nonExistentEventId = 99999L

        assertFailsWith<IllegalArgumentException> {
            memberEventUpdater.markAsRead(nonExistentEventId, memberId)
        }.let {
            assertEquals("이벤트를 찾을 수 없거나 접근 권한이 없습니다: $nonExistentEventId", it.message)
        }
    }

    @Test
    fun `markAsRead - failure - throws exception when trying to access another member's event`() {
        val member1Id = 1L
        val member2Id = 2L

        // member1의 이벤트 생성
        val event = memberEventCreator.createEvent(
            memberId = member1Id,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "member1의 알림",
            message = "member1의 메시지"
        )

        // member2가 member1의 이벤트에 접근 시도
        assertFailsWith<IllegalArgumentException> {
            memberEventUpdater.markAsRead(event.requireId(), member2Id)
        }.let {
            assertEquals("이벤트를 찾을 수 없거나 접근 권한이 없습니다: ${event.requireId()}", it.message)
        }
    }

    @Test
    fun `deleteOldEvents - success - deletes events before specified date`() {
        val memberId = 1L
        val now = LocalDateTime.now()
        val oneWeekAgo = now.minusWeeks(1)
        val twoWeeksAgo = now.minusWeeks(2)
        val threeWeeksAgo = now.minusWeeks(3)

        // 특정 시간에 이벤트 생성
        val oldEvent1 = createEventWithCreatedAt(memberId, threeWeeksAgo, "오래된 알림 1")
        val oldEvent2 = createEventWithCreatedAt(memberId, twoWeeksAgo, "오래된 알림 2")
        val recentEvent = createEventWithCreatedAt(memberId, oneWeekAgo, "최신 알림")

        // oneWeekAgo보다 이전의 이벤트들 삭제
        val deletedCount = memberEventUpdater.deleteOldEvents(memberId, oneWeekAgo)

        // 2개의 이벤트가 삭제되어야 함
        assertEquals(2, deletedCount)

        // 최신 이벤트는 남아있어야 함
        val remainingEvents = memberEventRepository.findByMemberId(memberId)
        assertEquals(1, remainingEvents.size)
        assertEquals(recentEvent.requireId(), remainingEvents[0].requireId())
    }

    @Test
    fun `deleteOldEvents - success - returns 0 when no events to delete`() {
        val memberId = 1L
        val beforeDate = LocalDateTime.now().minusDays(1)

        // 현재 시간에 이벤트 생성
        memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "알림",
            message = "메시지"
        )

        // 이전 날짜로 삭제 시도 (삭제될 이벤트 없음)
        val deletedCount = memberEventUpdater.deleteOldEvents(memberId, beforeDate)

        assertEquals(0, deletedCount)
    }

    @Test
    fun `deleteOldEvents - success - returns 0 when member has no events`() {
        val memberId = 999L
        val pastDate = LocalDateTime.now().minusYears(1)

        val deletedCount = memberEventUpdater.deleteOldEvents(memberId, pastDate)

        assertEquals(0, deletedCount)
    }

    @Test
    fun `deleteOldEvents - success - deletes all events when date is far in future`() {
        val memberId = 1L
        val futureDate = LocalDateTime.now().plusYears(1)

        // 여러 이벤트 생성
        repeat(5) { index ->
            memberEventCreator.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "알림 $index",
                message = "메시지 $index"
            )
        }

        // 미래 날짜로 모든 이벤트 삭제
        val deletedCount = memberEventUpdater.deleteOldEvents(memberId, futureDate)

        assertEquals(5, deletedCount)
    }

    @Test
    fun `integration - all methods work together correctly`() {
        val memberId = 1L
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)
        val twoDaysAgo = now.minusDays(2)

        // 이벤트 생성
        val event1 = createEventWithCreatedAt(memberId, twoDaysAgo, "알림 1")
        val event2 = createEventWithCreatedAt(memberId, yesterday, "알림 2")

        // 하나만 읽음으로 표시
        memberEventUpdater.markAsRead(event1.requireId(), memberId)

        // 나머지 모두 읽음으로 표시
        val markedCount = memberEventUpdater.markAllAsRead(memberId)
        assertEquals(1, markedCount) // event2만 읽음으로 표시됨

        // 어제 이전의 이벤트 삭제
        val deletedCount = memberEventUpdater.deleteOldEvents(memberId, yesterday)
        assertEquals(1, deletedCount) // event1만 삭제됨
    }

    @Test
    fun `deleteOldEvents - success - deletes only specified member's events`() {
        val member1Id = 1L
        val member2Id = 2L
        val now = LocalDateTime.now()
        val oneWeekAgo = now.minusWeeks(1)
        val twoWeeksAgo = now.minusWeeks(2)

        // member1의 오래된 이벤트 생성
        val member1OldEvent = createEventWithCreatedAt(member1Id, twoWeeksAgo, "member1의 오래된 알림")
        val member1RecentEvent = createEventWithCreatedAt(member1Id, oneWeekAgo, "member1의 최신 알림")

        // member2의 오래된 이벤트 생성
        val member2OldEvent = createEventWithCreatedAt(member2Id, twoWeeksAgo, "member2의 오래된 알림")

        // member1의 오래된 이벤트만 삭제
        val deletedCount = memberEventUpdater.deleteOldEvents(member1Id, oneWeekAgo)

        // member1의 오래된 이벤트 1개만 삭제됨
        assertEquals(1, deletedCount)

        // member2의 이벤트은 그대로 있어야 함
        val member2Events = memberEventRepository.findByMemberId(member2Id)
        assertEquals(1, member2Events.size)
        assertEquals(member2OldEvent.requireId(), member2Events[0].requireId())

        // member1의 최신 이벤트은 남아있어야 함
        val member1Events = memberEventRepository.findByMemberId(member1Id)
        assertEquals(1, member1Events.size)
        assertEquals(member1RecentEvent.requireId(), member1Events[0].requireId())
    }

    // Helper method to create events with specific createdAt timestamp
    private fun createEventWithCreatedAt(
        memberId: Long,
        createdAt: LocalDateTime,
        title: String,
        relatedMemberId: Long? = null,
        relatedPostId: Long? = null,
        relatedCommentId: Long? = null,
    ): MemberEvent {
        // Test helper class that allows setting createdAt directly
        val event = MemberEvent.create(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = title,
            message = "메시지",
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId,
        )
        event.setCreatedAt(createdAt)
        return memberEventRepository.save(event)
    }

    private fun MemberEvent.setCreatedAt(createdAt: LocalDateTime) {
        val fieldCreatedAt = MemberEvent::class.java.getDeclaredField("createdAt")
        fieldCreatedAt.isAccessible = true
        fieldCreatedAt.set(this, createdAt)
    }
}
