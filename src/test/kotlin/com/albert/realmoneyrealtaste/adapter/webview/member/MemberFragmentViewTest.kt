package com.albert.realmoneyrealtaste.adapter.webview.member

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

/**
 * MemberFragmentView 테스트
 */
class MemberFragmentViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readSidebarFragment - success - returns suggested users sidebar fragment`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT))
            .andExpect(model().attributeExists("suggestedUsers"))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `memberProfileFragment - success - returns member profile fragment`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(get(MemberUrls.FRAGMENT_MEMBER_PROFILE))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.MEMBER_PROFILE_FRAGMENT))
            .andExpect(model().attributeExists("member"))
    }
}
