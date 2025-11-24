package com.albert.realmoneyrealtaste.adapter.webapi.follow

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FollowReadApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var followCreator: FollowCreator

    @WithMockMember
    @Test
    fun `getFollowStats - success - returns follow statistics for existing member`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val follower1 = testMemberHelper.createActivatedMember("follower1@example.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@example.com", "follower2")
        val following1 = testMemberHelper.createActivatedMember("following1@example.com", "following1")

        // 팔로우 관계 설정
        followCreator.follow(FollowCreateRequest(follower1.requireId(), targetMember.requireId()))
        followCreator.follow(FollowCreateRequest(follower2.requireId(), targetMember.requireId()))
        followCreator.follow(FollowCreateRequest(targetMember.requireId(), following1.requireId()))

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/follow-stats"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.followersCount").value(2))
            .andExpect(jsonPath("$.followingCount").value(1))
    }

    @WithMockMember
    @Test
    fun `getFollowStats - success - returns zero stats for member with no follows`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/follow-stats"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.followersCount").value(0))
            .andExpect(jsonPath("$.followingCount").value(0))
    }

    @WithMockMember
    @Test
    fun `getFollowStats - success - returns empty list for non-existent member`() {
        mockMvc.perform(get("/api/members/9999/follow-stats"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.followersCount").value(0))
            .andExpect(jsonPath("$.followingCount").value(0))
    }

    @WithMockMember
    @Test
    fun `getFollowings - success - returns all followings without keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val following1 = testMemberHelper.createActivatedMember("following1@example.com", "following1")
        val following2 = testMemberHelper.createActivatedMember("following2@example.com", "following2")

        // 팔로우 관계 설정
        followCreator.follow(FollowCreateRequest(targetMember.requireId(), following1.requireId()))
        followCreator.follow(FollowCreateRequest(targetMember.requireId(), following2.requireId()))

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followings"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("팔로잉 목록 조회 성공"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(2))
    }

    @WithMockMember
    @Test
    fun `getFollowings - success - returns filtered followings with keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val following1 = testMemberHelper.createActivatedMember("searchable@example.com", "searchable")
        val following2 = testMemberHelper.createActivatedMember("other@example.com", "other")

        // 팔로우 관계 설정
        followCreator.follow(FollowCreateRequest(targetMember.requireId(), following1.requireId()))
        followCreator.follow(FollowCreateRequest(targetMember.requireId(), following2.requireId()))

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followings?keyword=search"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
    }

    @WithMockMember
    @Test
    fun `getFollowings - success - returns empty list with empty keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followings?keyword="))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
    }

    @WithMockMember
    @Test
    fun `getFollowings - success - returns paginated results`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 여러 팔로잉 생성
        repeat(5) { index ->
            val following = testMemberHelper.createActivatedMember("following$index@example.com", "following$index")
            followCreator.follow(FollowCreateRequest(targetMember.requireId(), following.requireId()))
        }

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followings?page=0&size=3"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
            .andExpect(jsonPath("$.data.size").value(3))
            .andExpect(jsonPath("$.data.totalElements").value(5))
            .andExpect(jsonPath("$.data.totalPages").value(2))
    }

    @WithMockMember
    @Test
    fun `getFollowings - success - returns empty list for non-existent member`() {
        mockMvc.perform(get("/api/members/9999/followings"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
    }

    @WithMockMember
    @Test
    fun `getFollowers - success - returns all followers without keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val follower1 = testMemberHelper.createActivatedMember("follower1@example.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@example.com", "follower2")

        // 팔로우 관계 설정
        followCreator.follow(FollowCreateRequest(follower1.requireId(), targetMember.requireId()))
        followCreator.follow(FollowCreateRequest(follower2.requireId(), targetMember.requireId()))

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followers"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("팔로워 목록 조회 성공"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(2))
    }

    @WithMockMember
    @Test
    fun `getFollowers - success - returns filtered followers with keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val follower1 = testMemberHelper.createActivatedMember("searchable@example.com", "searchable")
        val follower2 = testMemberHelper.createActivatedMember("other@example.com", "other")

        // 팔로우 관계 설정
        followCreator.follow(FollowCreateRequest(follower1.requireId(), targetMember.requireId()))
        followCreator.follow(FollowCreateRequest(follower2.requireId(), targetMember.requireId()))

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followers?keyword=search"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].followerId").value(follower1.requireId()))
    }

    @WithMockMember
    @Test
    fun `getFollowers - success - returns empty list with empty keyword`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followers?keyword="))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
    }

    @WithMockMember
    @Test
    fun `getFollowers - success - returns paginated results`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 여러 팔로워 생성
        repeat(5) { index ->
            val follower = testMemberHelper.createActivatedMember("follower$index@example.com", "follower$index")
            followCreator.follow(FollowCreateRequest(follower.requireId(), targetMember.requireId()))
        }

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/followers?page=0&size=3"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3))
            .andExpect(jsonPath("$.data.size").value(3))
            .andExpect(jsonPath("$.data.totalElements").value(5))
            .andExpect(jsonPath("$.data.totalPages").value(2))
    }

    @WithMockMember
    @Test
    fun `getFollowers - success - returns empty list for non-existent member`() {
        mockMvc.perform(get("/api/members/9999/followers"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
    }
}
