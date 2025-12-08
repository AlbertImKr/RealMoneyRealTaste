package com.albert.realmoneyrealtaste.adapter.webview.event

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestEventHelper
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberEventViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var testEventHelper: TestEventHelper

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - returns events fragment for own events`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // 테스트 이벤트 생성
        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "새 게시물을 작성했습니다"
        )

        // When & Then
        mockMvc.get("/members/${member.requireId()}/events/fragment") {
            with(csrf())
            param("eventType", "POST_CREATED")
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attributeExists("member") }
            model { attributeExists("author") }
            model { attributeExists("events") }
            model { attributeExists("page") }
            model { attributeExists("eventType") }
            model {
                attribute("member", hasProperty<Long>("id", equalTo(member.requireId())))
            }
            model {
                attribute("author", hasProperty<Long>("id", equalTo(member.requireId())))
            }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - redirects when trying to view other member's events`() {
        // Given
        val member1 = testMemberHelper.getDefaultMember()
        val member2 = testMemberHelper.createActivatedMember("other@email.com")

        // When & Then - member1이 member2의 이벤트를 보려고 시도
        mockMvc.get("/members/${member2.requireId()}/events/fragment") {
            with(csrf())
        }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl("/members/${member1.requireId()}/events/fragment")
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - filters events by valid type`() {
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

        // When & Then - POST_CREATED 타입으로 필터링
        mockMvc.get("/members/${member.requireId()}/events/fragment?eventType=POST_CREATED") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attribute("eventType", "POST_CREATED") }
            model { attributeExists("events") }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - handles invalid eventType gracefully`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "게시물 생성"
        )

        // When & Then - 잘못된 eventType으로 요청
        mockMvc.get("/members/${member.requireId()}/events/fragment?eventType=INVALID_TYPE") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attribute("eventType", "INVALID_TYPE") }
            model { attributeExists("events") }
            // 모든 이벤트가 반환되어야 함 (fallback 동작)
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - respects pagination parameters`() {
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

        // When & Then - 페이지네이션 파라미터 적용
        mockMvc.get("/members/${member.requireId()}/events/fragment?page=0&size=2") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attributeExists("page") }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - returns empty fragment when no events`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.get("/members/${member.requireId()}/events/fragment") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attributeExists("events") }
            model { attribute("eventType", null) }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - works with different event types`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.FRIEND_REQUEST_SENT,
            title = "친구 요청",
            message = "친구 요청을 보냈습니다"
        )

        // When & Then - FRIEND_REQUEST_SENT 타입 (소문자로 전달)
        mockMvc.get("/members/${member.requireId()}/events/fragment?eventType=friend_request_sent") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attribute("eventType", "friend_request_sent") }
            model { attributeExists("events") }
        }
    }

    @Test
    fun `eventsFragment - failure - requires authentication`() {
        // Given
        val memberId = 1L

        // When & Then - 인증 없이 요청
        mockMvc.get("/members/$memberId/events/fragment") {
            with(csrf())
        }.andExpect {
            status { isForbidden() }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - handles case insensitive eventType`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "게시물 생성"
        )

        // When & Then - 소문자로 eventType 전달
        mockMvc.get("/members/${member.requireId()}/events/fragment?eventType=post_created") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
            model { attribute("eventType", "post_created") }
            model { attributeExists("events") }
        }
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `eventsFragment - success - returns correct model attributes structure`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        testEventHelper.createEvent(
            memberId = member.requireId(),
            eventType = MemberEventType.POST_CREATED,
            title = "게시물 생성",
            message = "게시물 생성"
        )

        // When & Then
        val result = mockMvc.get("/members/${member.requireId()}/events/fragment") {
            with(csrf())
        }.andExpect {
            status { isOk() }
            view { name("event/fragments/events :: events") }
        }.andReturn()

        val modelAndView = result.modelAndView
        val model = modelAndView!!.modelMap

        // 모든 필수 속성이 있는지 확인
        assertTrue(model.containsAttribute("member"))
        assertTrue(model.containsAttribute("author"))
        assertTrue(model.containsAttribute("events"))
        assertTrue(model.containsAttribute("page"))
        assertTrue(model.containsAttribute("eventType"))

        // member와 author가 동일한 객체인지 확인
        assertEquals(model["member"], model["author"])
    }
}
