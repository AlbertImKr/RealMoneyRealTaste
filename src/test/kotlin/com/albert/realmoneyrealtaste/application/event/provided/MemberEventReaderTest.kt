package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import org.springframework.data.domain.PageRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberEventReaderTest(
    val memberEventCreator: MemberEventCreator,
    val memberEventReader: MemberEventReader,
    val memberEventUpdater: MemberEventUpdater,
) : IntegrationTestBase() {

    @Test
    fun `readMemberEvents - success - returns paginated events ordered by createdAt desc`() {
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

        val event3 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.COMMENT_CREATED,
            title = "세 번째 알림",
            message = "세 번째 메시지"
        )

        // 페이지 크기 2로 조회
        val pageable = PageRequest.of(0, 2)
        val result = memberEventReader.readMemberEvents(memberId, pageable)

        // 페이지 정보 확인
        assertEquals(2, result.size)
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertEquals(0, result.number)
        assertTrue(result.hasNext())
        assertFalse(result.hasPrevious())

        // 최신순 정렬 확인 (event3, event2)
        val events = result.content
        assertEquals(event3.requireId(), events[0].id)
        assertEquals(event2.requireId(), events[1].id)

        // DTO 매핑 확인
        assertEquals(MemberEventType.COMMENT_CREATED, events[0].eventType)
        assertEquals("세 번째 알림", events[0].title)
        assertEquals("세 번째 메시지", events[0].message)
        assertFalse(events[0].isRead)
        assertNotNull(events[0].createdAt)
        assertEquals(null, events[0].relatedMemberId)
        assertEquals(null, events[0].relatedPostId)
        assertEquals(null, events[0].relatedCommentId)
    }

    @Test
    fun `readMemberEvents - success - returns empty page when member has no events`() {
        val memberId = 999L
        val pageable = PageRequest.of(0, 10)

        val result = memberEventReader.readMemberEvents(memberId, pageable)

        assertTrue(result.isEmpty)
        assertEquals(10, result.size)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.totalPages)
        assertEquals(0, result.number)
        assertFalse(result.hasNext())
        assertFalse(result.hasPrevious())
    }

    @Test
    fun `readMemberEvents - success - handles pagination correctly`() {
        val memberId = 1L

        // 5개 이벤트 생성
        repeat(5) { index ->
            memberEventCreator.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "알림 $index",
                message = "메시지 $index"
            )
        }

        // 첫 페이지 (크기 2)
        val firstPage = memberEventReader.readMemberEvents(memberId, PageRequest.of(0, 2))
        assertEquals(2, firstPage.size)
        assertEquals(5, firstPage.totalElements)
        assertEquals(3, firstPage.totalPages)

        // 두 번째 페이지
        val secondPage = memberEventReader.readMemberEvents(memberId, PageRequest.of(1, 2))
        assertEquals(2, secondPage.size)
        assertEquals(5, secondPage.totalElements)
        assertEquals(3, secondPage.totalPages)

        // 세 번째 페이지 (마지막)
        val thirdPage = memberEventReader.readMemberEvents(memberId, PageRequest.of(2, 2))
        assertEquals(2, thirdPage.size)
        assertEquals(5, thirdPage.totalElements)
        assertEquals(3, thirdPage.totalPages)

        // 네 번째 페이지 (범위 초과)
        val fourthPage = memberEventReader.readMemberEvents(memberId, PageRequest.of(3, 2))
        assertTrue(fourthPage.isEmpty)
    }

    @Test
    fun `readMemberEventsByType - success - filters events by type`() {
        val memberId = 1L

        // 다양한 타입의 이벤트 생성
        val friendEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 알림",
            message = "친구 메시지"
        )

        val postEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 알림",
            message = "게시물 메시지"
        )

        val anotherFriendEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
            title = "친구 알림 2",
            message = "친구 메시지 2"
        )

        // 친구 관련 이벤트만 필터링
        val friendEvents = memberEventReader.readMemberEventsByType(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            PageRequest.of(0, 10)
        )

        assertEquals(10, friendEvents.size)
        assertEquals(1, friendEvents.totalElements)
        assertEquals(friendEvent.requireId(), friendEvents.content[0].id)
        assertEquals(MemberEventType.FRIEND_REQUEST_SENT, friendEvents.content[0].eventType)

        // 게시물 관련 이벤트만 필터링
        val postEvents = memberEventReader.readMemberEventsByType(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            PageRequest.of(0, 10)
        )

        assertEquals(1, postEvents.totalElements)
        assertEquals(postEvent.requireId(), postEvents.content[0].id)
    }

    @Test
    fun `readMemberEventsByType - success - returns empty when no events of specified type`() {
        val memberId = 1L

        // 친구 이벤트만 생성
        memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 알림",
            message = "친구 메시지"
        )

        // 게시물 이벤트 조회 (없음)
        val postEvents = memberEventReader.readMemberEventsByType(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            PageRequest.of(0, 10)
        )

        assertTrue(postEvents.isEmpty)
        assertEquals(0, postEvents.totalElements)
    }

    @Test
    fun `readMemberEventsByType - success - maintains ordering by createdAt desc`() {
        val memberId = 1L

        // 같은 타입의 여러 이벤트 생성
        val event1 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "첫 번째",
            message = "메시지 1"
        )

        val event2 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "두 번째",
            message = "메시지 2"
        )

        val event3 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "세 번째",
            message = "메시지 3"
        )

        // 조회
        val events = memberEventReader.readMemberEventsByType(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            PageRequest.of(0, 10)
        )

        assertEquals(3, events.totalElements)
        // 최신순 정렬 확인
        assertEquals(event3.requireId(), events.content[0].id)
        assertEquals(event2.requireId(), events.content[1].id)
        assertEquals(event1.requireId(), events.content[2].id)
    }

    @Test
    fun `readUnreadEventCount - success - returns count of unread events`() {
        val memberId = 1L

        // 5개 이벤트 생성
        repeat(5) { index ->
            memberEventCreator.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "알림 $index",
                message = "메시지 $index"
            )
        }

        // 모두 안 읽음 상태
        val unreadCount = memberEventReader.readUnreadEventCount(memberId)
        assertEquals(5, unreadCount)

        // 2개 읽음으로 표시
        val events = memberEventReader.readMemberEvents(memberId, PageRequest.of(0, 5))
        memberEventUpdater.markAsRead(events.content[0].id, memberId)
        memberEventUpdater.markAsRead(events.content[1].id, memberId)

        // 3개 안 읽음
        val unreadCountAfter = memberEventReader.readUnreadEventCount(memberId)
        assertEquals(3, unreadCountAfter)
    }

    @Test
    fun `readUnreadEventCount - success - returns 0 when all events are read`() {
        val memberId = 1L

        // 이벤트 생성
        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "알림",
            message = "메시지"
        )

        // 읽음으로 표시
        memberEventUpdater.markAsRead(event.requireId(), memberId)

        // 안 읽은 이벤트 수 확인
        val unreadCount = memberEventReader.readUnreadEventCount(memberId)
        assertEquals(0, unreadCount)
    }

    @Test
    fun `readUnreadEventCount - success - returns 0 when member has no events`() {
        val memberId = 999L

        val unreadCount = memberEventReader.readUnreadEventCount(memberId)
        assertEquals(0, unreadCount)
    }

    @Test
    fun `readUnreadEventCount - success - counts only specific member's events`() {
        val member1Id = 1L
        val member2Id = 2L

        // member1의 이벤트
        memberEventCreator.createEvent(
            memberId = member1Id,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "member1 알림",
            message = "member1 메시지"
        )

        // member2의 이벤트
        memberEventCreator.createEvent(
            memberId = member2Id,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "member2 알림",
            message = "member2 메시지"
        )

        // 각 회원의 안 읽은 이벤트 수 확인
        assertEquals(1, memberEventReader.readUnreadEventCount(member1Id))
        assertEquals(1, memberEventReader.readUnreadEventCount(member2Id))
    }

    @Test
    fun `integration - all methods work together correctly`() {
        val memberId = 1L

        // 다양한 타입의 이벤트 생성
        val friendEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 알림",
            message = "친구 메시지"
        )

        val postEvent = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 알림",
            message = "게시물 메시지"
        )

        // 전체 이벤트 조회
        val allEvents = memberEventReader.readMemberEvents(memberId, PageRequest.of(0, 10))
        assertEquals(10, allEvents.size)
        assertEquals(2, allEvents.totalElements)

        // 타입별 조회
        val friendEvents = memberEventReader.readMemberEventsByType(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            PageRequest.of(0, 10)
        )
        assertEquals(1, friendEvents.totalElements)

        // 안 읽은 이벤트 수 확인
        assertEquals(2, memberEventReader.readUnreadEventCount(memberId))

        // 하나 읽음으로 표시
        memberEventUpdater.markAsRead(friendEvent.requireId(), memberId)

        // 안 읽은 이벤트 수 재확인
        assertEquals(1, memberEventReader.readUnreadEventCount(memberId))
    }
}
