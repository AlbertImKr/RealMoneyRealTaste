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
            .andExpect(model().attributeExists("author"))
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
            .andExpect(model().attributeExists("author"))
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
            .andExpect(model().attributeExists("author"))
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
}
