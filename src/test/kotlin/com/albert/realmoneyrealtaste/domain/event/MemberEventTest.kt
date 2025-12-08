package com.albert.realmoneyrealtaste.domain.event

import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberEventTest {

    @Test
    fun `create - success - creates event with all required properties`() {
        val memberId = 1L
        val eventType = MemberEventType.FRIEND_REQUEST_SENT
        val title = "친구 요청 알림"
        val message = "홍길동님이 친구 요청을 보냈습니다"
        val relatedMemberId = 2L
        val relatedPostId = null
        val relatedCommentId = null

        val event = MemberEvent.create(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )

        assertAll(
            { assertEquals(memberId, event.memberId) },
            { assertEquals(eventType, event.eventType) },
            { assertEquals(title, event.title) },
            { assertEquals(message, event.message) },
            { assertEquals(relatedMemberId, event.relatedMemberId) },
            { assertEquals(relatedPostId, event.relatedPostId) },
            { assertEquals(relatedCommentId, event.relatedCommentId) },
            { assertFalse(event.isRead) },
            { assertTrue(event.createdAt <= LocalDateTime.now()) },
            { assertTrue(event.createdAt > LocalDateTime.now().minusSeconds(5)) }
        )
    }

    @Test
    fun `create - success - creates event with all related IDs`() {
        val memberId = 1L
        val eventType = MemberEventType.POST_COMMENTED
        val title = "댓글 알림"
        val message = "게시물에 댓글이 달렸습니다"
        val relatedMemberId = 2L
        val relatedPostId = 100L
        val relatedCommentId = 200L

        val event = MemberEvent.create(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )

        assertAll(
            { assertEquals(memberId, event.memberId) },
            { assertEquals(eventType, event.eventType) },
            { assertEquals(title, event.title) },
            { assertEquals(message, event.message) },
            { assertEquals(relatedMemberId, event.relatedMemberId) },
            { assertEquals(relatedPostId, event.relatedPostId) },
            { assertEquals(relatedCommentId, event.relatedCommentId) }
        )
    }

    @Test
    fun `create - failure - throws exception when memberId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 0L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "메시지"
            )
        }.let {
            assertEquals(MemberEvent.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when memberId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = -1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "메시지"
            )
        }.let {
            assertEquals(MemberEvent.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when title is empty`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "",
                message = "메시지"
            )
        }.let {
            assertEquals(MemberEvent.ERROR_TITLE_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when title is blank`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "   ",
                message = "메시지"
            )
        }.let {
            assertEquals(MemberEvent.ERROR_TITLE_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when message is empty`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = ""
            )
        }.let {
            assertEquals(MemberEvent.ERROR_MESSAGE_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when message is blank`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "제목",
                message = "\t\n\r "
            )
        }.let {
            assertEquals(MemberEvent.ERROR_MESSAGE_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when both title and message are blank`() {
        assertFailsWith<IllegalArgumentException> {
            MemberEvent.create(
                memberId = 1L,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "",
                message = ""
            )
        }.let {
            // title 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(MemberEvent.ERROR_TITLE_MUST_NOT_BE_EMPTY, it.message)
        }
    }

    @Test
    fun `markAsRead - success - marks event as read`() {
        val event = MemberEvent.create(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "제목",
            message = "메시지"
        )

        // 초기 상태 확인
        assertFalse(event.isRead)

        // 읽음으로 표시
        event.markAsRead()

        // 변경 확인
        assertTrue(event.isRead)
    }

    @Test
    fun `markAsRead - success - can be called multiple times`() {
        val event = MemberEvent.create(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "제목",
            message = "메시지"
        )

        event.markAsRead()
        assertTrue(event.isRead)

        // 다시 호출해도 상태는 유지됨
        event.markAsRead()
        assertTrue(event.isRead)
    }

    @Test
    fun `create - success - handles edge case values`() {
        val event = MemberEvent.create(
            memberId = Long.MAX_VALUE,
            eventType = MemberEventType.ACCOUNT_ACTIVATED,
            title = "a",
            message = "b",
            relatedMemberId = Long.MAX_VALUE,
            relatedPostId = Long.MAX_VALUE,
            relatedCommentId = Long.MAX_VALUE
        )

        assertAll(
            { assertEquals(Long.MAX_VALUE, event.memberId) },
            { assertEquals(MemberEventType.ACCOUNT_ACTIVATED, event.eventType) },
            { assertEquals("a", event.title) },
            { assertEquals("b", event.message) },
            { assertEquals(Long.MAX_VALUE, event.relatedMemberId) },
            { assertEquals(Long.MAX_VALUE, event.relatedPostId) },
            { assertEquals(Long.MAX_VALUE, event.relatedCommentId) }
        )
    }

    @Test
    fun `create - success - handles Korean characters`() {
        val event = createMemberEvent(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 요청 알림",
            message = "홍길동님이 친구 요청을 보냈습니다"
        )

        assertAll(
            { assertEquals("친구 요청 알림", event.title) },
            { assertEquals("홍길동님이 친구 요청을 보냈습니다", event.message) }
        )
    }

    @Test
    fun `create - success - handles special characters in title and message`() {
        val event = createMemberEvent(
            memberId = 1L,
            eventType = MemberEventType.POST_CREATED,
            title = "Special Event! @#$%^&*()",
            message = "Message with special chars: 안녕~ Hello! @#$% &*()"
        )

        assertAll(
            { assertEquals("Special Event! @#$%^&*()", event.title) },
            { assertEquals("Message with special chars: 안녕~ Hello! @#$% &*()", event.message) }
        )
    }

    @Test
    fun `create - success - handles minimum valid memberId`() {
        val event = MemberEvent.create(
            memberId = 1L,
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 작성",
            message = "새 게시물이 작성되었습니다"
        )

        assertEquals(1L, event.memberId)
    }

    @Test
    fun `create - success - all event types are supported`() {
        MemberEventType.values().forEach { eventType ->
            val event = MemberEvent.create(
                memberId = 1L,
                eventType = eventType,
                title = "제목",
                message = "메시지"
            )
            assertEquals(eventType, event.eventType)
        }
    }

    @Test
    fun `create - success - default values are correctly set`() {
        val before = LocalDateTime.now()

        val event = MemberEvent.create(
            memberId = 1L,
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "제목",
            message = "메시지"
        )

        val after = LocalDateTime.now()

        assertAll(
            { assertFalse(event.isRead) },
            { assertTrue(event.createdAt >= before) },
            { assertTrue(event.createdAt <= after) }
        )
    }

    @Test
    fun `create - success - creates event without related IDs`() {
        val event = MemberEvent.create(
            memberId = 1L,
            eventType = MemberEventType.ACCOUNT_ACTIVATED,
            title = "계정 활성화",
            message = "계정이 활성화되었습니다"
        )

        assertAll(
            { assertNull(event.relatedMemberId) },
            { assertNull(event.relatedPostId) },
            { assertNull(event.relatedCommentId) }
        )
    }

    @Test
    fun `constants - success - has correct error messages`() {
        assertAll(
            { assertEquals("회원 ID는 양수여야 합니다", MemberEvent.ERROR_MEMBER_ID_MUST_BE_POSITIVE) },
            { assertEquals("제목은 비어있을 수 없습니다", MemberEvent.ERROR_TITLE_MUST_NOT_BE_EMPTY) },
            { assertEquals("메시지는 비어있을 수 없습니다", MemberEvent.ERROR_MESSAGE_MUST_NOT_BE_EMPTY) }
        )
    }

    @Test
    fun `setters - success - update properties for code coverage`() {
        val event = TestMemberEvent()
        val newMemberId = 20L
        val newEventType = MemberEventType.FRIEND_REQUEST_ACCEPTED
        val newRelatedMemberId = 30L
        val newRelatedPostId = 40L
        val newRelatedCommentId = 25L
        val newCreatedAt = LocalDateTime.now()
        val newTitle = "새로운 제목"
        val newMessage = "새로운 메시지"
        val newIsRead = true

        event.setMemberIdForTest(newMemberId)
        event.setEventTypeForTest(newEventType)
        event.setTitleForTest(newTitle)
        event.setMessageForTest(newMessage)
        event.setRelatedMemberIdForTest(newRelatedMemberId)
        event.setRelatedPostIdForTest(newRelatedPostId)
        event.setRelatedCommentIdForTest(newRelatedCommentId)
        event.setIsReadForTest(newIsRead)
        event.setCreatedAtForTest(newCreatedAt)

        assertEquals(newMemberId, event.memberId)
        assertEquals(newEventType, event.eventType)
        assertEquals(newTitle, event.title)
        assertEquals(newMessage, event.message)
        assertEquals(newRelatedMemberId, event.relatedMemberId)
        assertEquals(newRelatedPostId, event.relatedPostId)
        assertEquals(newRelatedCommentId, event.relatedCommentId)
        assertEquals(newIsRead, event.isRead)
        assertEquals(newCreatedAt, event.createdAt)
    }

    // Helper method to reduce test boilerplate
    private fun createMemberEvent(
        memberId: Long = 1L,
        eventType: MemberEventType = MemberEventType.FRIEND_REQUEST_SENT,
        title: String = "제목",
        message: String = "메시지",
        relatedMemberId: Long? = null,
        relatedPostId: Long? = null,
        relatedCommentId: Long? = null,
    ): MemberEvent {
        return MemberEvent.create(
            memberId = memberId,
            eventType = eventType,
            title = title,
            message = message,
            relatedMemberId = relatedMemberId,
            relatedPostId = relatedPostId,
            relatedCommentId = relatedCommentId
        )
    }

    private class TestMemberEvent : MemberEvent(
        memberId = 1L,
        eventType = MemberEventType.FRIEND_REQUEST_SENT,
        title = "제목",
        message = "메시지",
        relatedMemberId = null,
        relatedPostId = null,
        relatedCommentId = null,
        isRead = false,
        createdAt = LocalDateTime.now()
    ) {
        fun setMemberIdForTest(memberId: Long) {
            this.memberId = memberId
        }

        fun setEventTypeForTest(eventType: MemberEventType) {
            this.eventType = eventType
        }

        fun setTitleForTest(title: String) {
            this.title = title
        }

        fun setMessageForTest(message: String) {
            this.message = message
        }

        fun setRelatedMemberIdForTest(relatedMemberId: Long?) {
            this.relatedMemberId = relatedMemberId
        }

        fun setRelatedPostIdForTest(relatedPostId: Long?) {
            this.relatedPostId = relatedPostId
        }

        fun setRelatedCommentIdForTest(relatedCommentId: Long?) {
            this.relatedCommentId = relatedCommentId
        }

        fun setIsReadForTest(isRead: Boolean) {
            this.isRead = isRead
        }

        fun setCreatedAtForTest(createdAt: LocalDateTime) {
            this.createdAt = createdAt
        }
    }
}
