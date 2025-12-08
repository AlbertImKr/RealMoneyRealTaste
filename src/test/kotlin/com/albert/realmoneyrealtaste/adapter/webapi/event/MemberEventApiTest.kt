package com.albert.realmoneyrealtaste.adapter.webapi.event

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.event.provided.MemberEventReader
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestEventHelper
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals

class MemberEventApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var memberEventReader: MemberEventReader

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testEventHelper: TestEventHelper

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEvents - success - returns paginated events`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 테스트 이벤트 생성
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "새 게시물을 작성했습니다"
        )
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.COMMENT_CREATED,
            title = "댓글 생성",
            message = "댓글을 작성했습니다"
        )

        // When & Then
        mockMvc.get("/api/v1/events") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { exists() }
            jsonPath("$.totalElements") { value(4) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEvents - success - respects pagination`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 5개 이벤트 생성
        repeat(5) { i ->
            testEventHelper.createEvent(
                memberId = member.requireId(),
                eventType = MemberEventType.POST_CREATED,
                title = "게시물 생성 $i",
                message = "메시지 $i"
            )
        }

        // When & Then - 첫 페이지
        mockMvc.get("/api/v1/events?page=0&size=2") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.size()") { value(2) }
            jsonPath("$.totalElements") { value(10) }
            jsonPath("$.totalPages") { value(5) }
            jsonPath("$.first") { value(true) }
            jsonPath("$.last") { value(false) }
        }

        // When & Then - 두 번째 페이지
        mockMvc.get("/api/v1/events?page=1&size=2") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(2) }
            jsonPath("$.first") { value(false) }
            jsonPath("$.last") { value(false) }
        }

        // When & Then - 세 번째 페이지
        mockMvc.get("/api/v1/events?page=2&size=2") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(2) }
            jsonPath("$.last") { value(false) }
        }

        // When & Then - 마지막 페이지
        mockMvc.get("/api/v1/events?page=4&size=2") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(2) }
            jsonPath("$.last") { value(true) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEventsByType - success - returns filtered events`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 다양한 타입의 이벤트 생성
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "게시물 생성"
        )
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성2",
            message = "게시물 생성2"
        )
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.COMMENT_CREATED,
            title = "댓글 생성",
            message = "댓글 생성"
        )

        // When & Then - POST_CREATED 타입만 필터링
        mockMvc.get("/api/v1/events/type/POST_CREATED") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.size()") { value(4) }
            jsonPath("$.content[0].eventType") { value("POST_CREATED") }
            jsonPath("$.content[1].eventType") { value("POST_CREATED") }
            jsonPath("$.totalElements") { value(4) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEventsByType - success - works with different event types`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 요청",
            message = "친구 요청을 보냈습니다"
        )

        // When & Then - FRIEND_REQUEST_SENT 타입
        mockMvc.get("/api/v1/events/type/FRIEND_REQUEST_SENT") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(2) }
            jsonPath("$.content[0].eventType") { value("FRIEND_REQUEST_SENT") }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getUnreadEventCount - success - returns correct count`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 읽지 않은 이벤트 3개 생성
        repeat(3) {
            testEventHelper.createEvent(
                memberId = member.requireId(),
                eventType = MemberEventType.POST_CREATED,
                title = "게시물 생성",
                message = "메시지",
            )
        }

        // 읽은 이벤트 2개 생성
        repeat(2) {
            testEventHelper.createEvent(
                memberId = member.requireId(),
                eventType = MemberEventType.COMMENT_CREATED,
                title = "댓글 생성",
                message = "메시지",
                isRead = true,
            )
        }

        // When & Then
        val result = mockMvc.get("/api/v1/events/unread-count") {
            with(csrf())
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val count = ObjectMapper().readValue<Long>(result)
        assertEquals(10L, count)
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getUnreadEventCount - success - returns zero when no unread events`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 모두 읽은 상태의 이벤트 생성
        repeat(2) {
            testEventHelper.createEvent(
                memberId = member.requireId(),
                eventType = MemberEventType.POST_CREATED,
                title = "게시물 생성",
                message = "메시지",
                isRead = true,
            )
        }

        // When & Then
        val result = mockMvc.get("/api/v1/events/unread-count") {
            with(csrf())
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val count = ObjectMapper().readValue<Long>(result)
        assertEquals(4L, count)
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `markAllAsRead - success - marks all events as read and returns count`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 읽지 않은 이벤트 5개 생성
        val events = repeat(5) {
            testEventHelper.createEvent(
                memberId = member.requireId(),
                eventType = MemberEventType.POST_CREATED,
                title = "게시물 생성",
                message = "메시지",
            )
        }

        // When & Then
        val result = mockMvc.post("/api/v1/events/mark-all-read") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val count = ObjectMapper().readValue<Int>(result)
        assertEquals(10, count)

        // DB에서 모두 읽음 상태인지 확인
        val unreadCount = memberEventReader.readUnreadEventCount(member.requireId())
        assertEquals(0L, unreadCount)
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `markAllAsRead - success - returns zero when no events to mark`() {
        // When & Then
        val result = mockMvc.post("/api/v1/events/mark-all-read") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        val count = ObjectMapper().readValue<Int>(result)
        assertEquals(0, count)
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `markAsRead - success - marks specific event as read`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        val event = testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "메시지",
        )

        // When & Then
        val result = mockMvc.post("/api/v1/events/${event.requireId()}/read") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(event.requireId()) }
            jsonPath("$.isRead") { value(true) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `markAsRead - error - returns 404 when event not found`() {
        // When & Then
        mockMvc.post("/api/v1/events/99999/read") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `markAsRead - error - returns 403 when trying to mark other member's event`() {
        // Given
        val member1 = testMemberHelper.createActivatedMember("other@email.com")

        val event = testEventHelper.createEvent(
            memberId = member1.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "메시지",
        )

        // When & Then - member2가 member1의 이벤트를 읽음으로 표시하려고 시도
        mockMvc.post("/api/v1/events/${event.requireId()}/read") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `all endpoints - error - require authentication`() {
        // When & Then - 인증 없이 요청
        mockMvc.get("/api/v1/events").andExpect {
            status { isForbidden() }
        }

        mockMvc.get("/api/v1/events/type/POST_CREATED").andExpect {
            status { isForbidden() }
        }

        mockMvc.get("/api/v1/events/unread-count").andExpect {
            status { isForbidden() }
        }

        mockMvc.post("/api/v1/events/mark-all-read") {
            with(csrf())
        }.andExpect {
            status { isForbidden() }
        }

        mockMvc.post("/api/v1/events/1/read") {
            with(csrf())
        }.andExpect {
            status { isForbidden() }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `POST endpoints - error - require CSRF protection`() {
        // When & Then - CSRF 없이 POST 요청
        mockMvc.post("/api/v1/events/mark-all-read") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isForbidden() }
        }

        mockMvc.post("/api/v1/events/1/read") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isForbidden() }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEvents - success - returns empty page when no events`() {
        // When & Then
        mockMvc.get("/api/v1/events") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.size()") { value(0) }
            jsonPath("$.totalElements") { value(0) }
            jsonPath("$.empty") { value(true) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `getMemberEventsByType - success - returns empty page when no events of type`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 다른 타입의 이벤트만 생성
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "메시지"
        )

        // When & Then - COMMENT_CREATED 타입으로 조회 (없는 타입)
        mockMvc.get("/api/v1/events/type/COMMENT_CREATED") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(0) }
            jsonPath("$.totalElements") { value(0) }
        }
    }
}
