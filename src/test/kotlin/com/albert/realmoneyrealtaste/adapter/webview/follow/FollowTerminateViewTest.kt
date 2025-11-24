package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import com.albert.realmoneyrealtaste.application.follow.provided.FollowReader
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertTrue

class FollowTerminateViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var followCreator: FollowCreator

    @Autowired
    private lateinit var followReader: FollowReader

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfollow - success - removes follow relationship and returns follow button HTML`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 먼저 팔로우 관계 생성
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember.requireId()
            )
        )

        val result = mockMvc.perform(delete("/members/${targetMember.requireId()}/follow").with(csrf()))
            .andExpect(status().isOk)
            .andReturn()

        val responseContent = result.response.contentAsString
        assertTrue(responseContent.contains("btn btn-primary-soft"), "언팔로우 성공 후 팔로우 버튼 스타일이 포함되어야 함")
        assertTrue(
            responseContent.contains("hx-post=\"/members/${targetMember.requireId()}/follow\""),
            "팔로우 요청 URL이 포함되어야 함"
        )
        assertTrue(responseContent.contains("fa-solid fa-plus"), "팔로우 추가 아이콘이 포함되어야 함")
        assertTrue(responseContent.contains("follow-button-${targetMember.requireId()}"), "타겟 ID가 포함되어야 함")

        // 실제 팔로우 관계가 삭제되었는지 확인
        val followings = followReader.findFollowingsByMemberId(
            memberId = testMemberHelper.getDefaultMember().requireId(),
            pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        )
        assertTrue(followings.content.none { it.followingId == targetMember.requireId() }, "팔로우 관계가 삭제되어야 함")
    }

    @Test
    fun `unfollow - forbidden - when not authenticated returns 403`() {
        mockMvc.perform(delete("/members/1/follow"))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfollow - failure - when target member does not exist returns 400 with error message`() {
        mockMvc.perform(delete("/members/9999/follow").with(csrf()))
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfollow - failure - when trying to unfollow self returns 400 with error message`() {
        val currentMember = testMemberHelper.getDefaultMember()

        mockMvc.perform(delete("/members/${currentMember.requireId()}/follow").with(csrf()))
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfollow - failure - when no follow relationship exists returns 400 with error message`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 팔로우하지 않은 상태에서 언팔로우 시도
        mockMvc.perform(delete("/members/${targetMember.requireId()}/follow").with(csrf()))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfollow - success - works correctly after multiple follow-unfollow cycles`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 여러 번 팔로우/언팔로우 반복
        repeat(3) {
            // 팔로우
            mockMvc.perform(post("/members/${targetMember.requireId()}/follow").with(csrf()))
                .andExpect(status().isOk)

            // 언팔로우
            val result = mockMvc.perform(delete("/members/${targetMember.requireId()}/follow").with(csrf()))
                .andExpect(status().isOk)
                .andReturn()

            val responseContent = result.response.contentAsString
            assertTrue(responseContent.contains("hx-post"), "반복적인 언팔로우 후에도 팔로우 버튼이 반환되어야 함")
        }
    }
}
