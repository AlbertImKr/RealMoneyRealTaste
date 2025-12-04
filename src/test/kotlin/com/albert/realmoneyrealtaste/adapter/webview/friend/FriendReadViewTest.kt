package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test
import kotlin.test.assertEquals

class FriendReadViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    fun `readFriendsPage - forbidden - when not authenticated`() {
        mockMvc.perform(
            get(FriendUrls.FRIENDS)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendWidgetFragment - success - returns friend widget for own profile`() {
        val member = testMemberHelper.getDefaultMember()

        mockMvc.perform(
            get(FriendUrls.FRIEND_WIDGET, member.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_WIDGET))
            .andExpect(model().attributeExists("recentFriends"))
            .andExpect(model().attributeExists("pendingRequestsCount"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readFriendWidgetFragment - success - returns friend widget for other profile without authentication`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(
            get(FriendUrls.FRIEND_WIDGET, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_WIDGET))
            .andExpect(model().attributeExists("recentFriends"))
            .andExpect(model().attributeExists("pendingRequestsCount"))
            .andExpect(model().attributeDoesNotExist("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendWidgetFragment - success - returns friend widget for other profile`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(
            get(FriendUrls.FRIEND_WIDGET, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_WIDGET))
            .andExpect(model().attributeExists("recentFriends"))
            .andExpect(model().attributeExists("pendingRequestsCount"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendRequestsFragment - success - returns friend requests fragment`() {
        mockMvc.perform(
            get(FriendUrls.FRIEND_REQUESTS_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_REQUESTS))
            .andExpect(model().attributeExists("pendingRequests"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readFriendRequestsFragment - forbidden - when not authenticated`() {
        mockMvc.perform(
            get(FriendUrls.FRIEND_REQUESTS_FRAGMENT)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    fun `readFriendButton - forbidden - when not authenticated`() {
        val authorId = 1L

        mockMvc.perform(
            get(FriendUrls.FRIEND_BUTTON, authorId)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendButton - success - returns friend button when not friend`() {
        val member = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember("author@test.com", "author")

        mockMvc.perform(
            get(FriendUrls.FRIEND_BUTTON, author.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("authorId"))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attribute("authorId", author.requireId()))
            .andExpect(model().attribute("isFriend", false))
            .andExpect(model().attribute("hasSentFriendRequest", false))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendWidgetFragment - success - pendingRequestsCount is zero for other profile`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result = mockMvc.perform(
            get(FriendUrls.FRIEND_WIDGET, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_WIDGET))
            .andExpect(model().attributeExists("recentFriends"))
            .andExpect(model().attributeExists("pendingRequestsCount"))
            .andExpect(model().attributeExists("member"))
            .andReturn()

        // 다른 사용자 프로필에서는 pendingRequestsCount가 항상 0이어야 함
        val modelAndView = result.modelAndView!!
        val pendingRequestsCount = modelAndView.model["pendingRequestsCount"] as Long
        assertEquals(0, pendingRequestsCount, "다른 사용자의 pendingRequestsCount는 0이어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFriendWidgetFragment - success - handles pagination correctly`() {
        val member = testMemberHelper.getDefaultMember()

        mockMvc.perform(
            get(FriendUrls.FRIEND_WIDGET, member.requireId())
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_WIDGET))
            .andExpect(model().attributeExists("recentFriends"))
            .andExpect(model().attributeExists("pendingRequestsCount"))
            .andExpect(model().attributeExists("member"))
    }
}
