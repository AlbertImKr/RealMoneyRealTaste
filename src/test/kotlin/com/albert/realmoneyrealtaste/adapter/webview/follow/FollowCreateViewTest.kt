package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertTrue

class FollowCreateViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var followReader: FollowReader

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `follow - success - creates follow and returns unfollow button HTML`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result = mockMvc.perform(
            post("/members/${targetMember.requireId()}/follow")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseContent = result.response.contentAsString
        assertTrue(responseContent.contains("btn btn-primary"), "팔로우 성공 후 언팔로우 버튼 스타일이 포함되어야 함")
        assertTrue(
            responseContent.contains("hx-delete=\"/members/${targetMember.requireId()}/follow\""),
            "언팔로우 요청 URL이 포함되어야 함"
        )
        assertTrue(responseContent.contains("bi-person-check-fill"), "팔로우 확인 아이콘이 포함되어야 함")
        assertTrue(responseContent.contains("follow-button-${targetMember.requireId()}"), "타겟 ID가 포함되어야 함")

        // 실제 팔로우 관계가 생성되었는지 확인
        val followings = followReader.findFollowingsByMemberId(
            memberId = testMemberHelper.getDefaultMember().requireId(),
            pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        )
        assertTrue(followings.content.any { it.followingId == targetMember.requireId() }, "팔로우 관계가 생성되어야 함")
    }

    @Test
    fun `follow - forbidden - when not authenticated`() {
        mockMvc.perform(post("/members/1/follow"))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `follow - success - handles invalid target member id gracefully`() {
        mockMvc.perform(post("/members/9999/follow"))
            .andExpect(status().is4xxClientError())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `follow - success - cannot follow self`() {
        val currentMember = testMemberHelper.getDefaultMember()

        mockMvc.perform(post("/members/${currentMember.requireId()}/follow"))
            .andExpect(status().is4xxClientError())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `follow - success - handles duplicate follow gracefully`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 첫 번째 팔로우
        mockMvc.perform(post("/members/${targetMember.requireId()}/follow").with(csrf()))
            .andExpect(status().isOk)

        // 중복 팔로우 시도
        val result = mockMvc.perform(post("/members/${targetMember.requireId()}/follow").with(csrf()))
            .andExpect(status().isOk)
            .andReturn()

        val responseContent = result.response.contentAsString
        assertTrue(responseContent.contains("hx-delete"), "중복 팔로우 시도 시에도 언팔로우 버튼이 반환되어야 함")
    }
}
