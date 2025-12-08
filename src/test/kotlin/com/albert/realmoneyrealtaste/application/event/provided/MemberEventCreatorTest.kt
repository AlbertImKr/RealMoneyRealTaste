package com.albert.realmoneyrealtaste.application.event.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemberEventCreatorTest(
    val memberEventCreator: MemberEventCreator,
) : IntegrationTestBase() {

    @Test
    fun `createEvent - success - creates friend request sent event`() {
        val memberId = 1L
        val eventType = MemberEventType.FRIEND_REQUEST_SENT
        val title = "친구 요청 알림"
        val message = "홍길동님이 친구 요청을 보냈습니다"
        val relatedMemberId = 2L

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId
        )

        assertNotNull(event)
        assertEquals(memberId, event.memberId)
        assertEquals(eventType, event.eventType)
        assertEquals(title, event.title)
        assertEquals(message, event.message)
        assertEquals(relatedMemberId, event.relatedMemberId)
        assertTrue(event.requireId() > 0) // 저장된 경우 ID가 할당됨
    }

    @Test
    fun `createEvent - success - creates post created event with all related IDs`() {
        val memberId = 1L
        val eventType = MemberEventType.POST_COMMENTED
        val title = "댓글 알림"
        val message = "게시물에 댓글이 달렸습니다"
        val relatedMemberId = 2L
        val relatedPostId = 100L
        val relatedCommentId = 200L

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )

        assertNotNull(event)
        assertEquals(memberId, event.memberId)
        assertEquals(eventType, event.eventType)
        assertEquals(title, event.title)
        assertEquals(message, event.message)
        assertEquals(relatedMemberId, event.relatedMemberId)
        assertEquals(relatedPostId, event.relatedPostId)
        assertEquals(relatedCommentId, event.relatedCommentId)
        assertTrue(event.requireId() > 0)
    }

    @Test
    fun `createEvent - success - creates account activated event without related IDs`() {
        val memberId = 1L
        val eventType = MemberEventType.ACCOUNT_ACTIVATED
        val title = "계정 활성화"
        val message = "계정이 활성화되었습니다"

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message
        )

        assertNotNull(event)
        assertEquals(memberId, event.memberId)
        assertEquals(eventType, event.eventType)
        assertEquals(title, event.title)
        assertEquals(message, event.message)
        assertEquals(null, event.relatedMemberId)
        assertEquals(null, event.relatedPostId)
        assertEquals(null, event.relatedCommentId)
        assertTrue(event.requireId() > 0)
    }

    @Test
    fun `createEvent - success - handles Korean characters in title and message`() {
        val memberId = 1L
        val eventType = MemberEventType.PROFILE_UPDATED
        val title = "프로필 업데이트 알림"
        val message = "김철수님이 프로필을 업데이트했습니다"

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message
        )

        assertNotNull(event)
        assertEquals(title, event.title)
        assertEquals(message, event.message)
        assertTrue(event.requireId() > 0)
    }

    @Test
    fun `createEvent - success - handles special characters in title and message`() {
        val memberId = 1L
        val eventType = MemberEventType.POST_CREATED
        val title = "New Post! @#$%^&*()"
        val message = "Special chars: 안녕~ Hello! @#$% &*()"

        val event = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message
        )

        assertNotNull(event)
        assertEquals(title, event.title)
        assertEquals(message, event.message)
        assertTrue(event.requireId() > 0)
    }

    @Test
    fun `createEvent - success - creates multiple events for same member`() {
        val memberId = 1L

        // 첫 번째 이벤트
        val event1 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "첫 번째 알림",
            message = "첫 번째 메시지"
        )

        // 두 번째 이벤트
        val event2 = memberEventCreator.createEvent(
            memberId = memberId,
            eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
            title = "두 번째 알림",
            message = "두 번째 메시지"
        )

        assertNotNull(event1)
        assertNotNull(event2)
        assertEquals(memberId, event1.memberId)
        assertEquals(memberId, event2.memberId)
        assertTrue(event1.requireId() > 0)
        assertTrue(event2.requireId() > 0)
        assertTrue(event1.requireId() != event2.requireId()) // 다른 ID를 가져야 함
    }

    @Test
    fun `createEvent - success - handles all event types`() {
        val memberId = 1L

        MemberEventType.entries.forEach { eventType ->
            val event = memberEventCreator.createEvent(
                memberId = memberId,
                eventType = eventType,
                title = "테스트 제목",
                message = "테스트 메시지"
            )

            assertNotNull(event)
            assertEquals(eventType, event.eventType)
            assertTrue(event.requireId() > 0)
        }
    }

    @Test
    fun `createEvent - success - isRead is false by default`() {
        val event = memberEventCreator.createEvent(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "제목",
            message = "메시지"
        )

        assertNotNull(event)
        assertEquals(false, event.isRead)
    }

    @Test
    fun `createEvent - success - createdAt is set automatically`() {
        val before = LocalDateTime.now()

        val event = memberEventCreator.createEvent(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "제목",
            message = "메시지"
        )

        val after = LocalDateTime.now()

        assertNotNull(event)
        assertTrue(event.createdAt >= before)
        assertTrue(event.createdAt <= after)
    }

    @Test
    fun `createEvent - failure - throws exception when memberId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = 0L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "메시지"
            )
        }.let {
            assertEquals("회원 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `createEvent - failure - throws exception when memberId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = -1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "메시지"
            )
        }.let {
            assertEquals("회원 ID는 양수여야 합니다", it.message)
        }
    }

    @Test
    fun `createEvent - failure - throws exception when title is empty`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "",
                message = "메시지"
            )
        }.let {
            assertEquals("제목은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `createEvent - failure - throws exception when title is blank`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "   ",
                message = "메시지"
            )
        }.let {
            assertEquals("제목은 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `createEvent - failure - throws exception when message is empty`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = ""
            )
        }.let {
            assertEquals("메시지는 비어있을 수 없습니다", it.message)
        }
    }

    @Test
    fun `createEvent - failure - throws exception when message is blank`() {
        assertFailsWith<IllegalArgumentException> {
            memberEventCreator.createEvent(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "\t\n\r "
            )
        }.let {
            assertEquals("메시지는 비어있을 수 없습니다", it.message)
        }
    }
}
