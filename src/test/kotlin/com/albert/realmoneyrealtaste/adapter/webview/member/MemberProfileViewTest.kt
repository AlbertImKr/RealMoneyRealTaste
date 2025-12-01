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
 * MemberProfileView 테스트
 */
class MemberProfileViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readProfile - success - returns profile view with correct attributes`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(get(MemberUrls.PROFILE, member.id))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readProfile - success - returns profile view when viewing other member's profile`() {
        // Given
        val otherMember = testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "OtherUser"
        )

        // When & Then
        mockMvc.perform(get(MemberUrls.PROFILE, otherMember.id))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `readProfile - failure - returns error when member is not active`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(get(MemberUrls.PROFILE, member.id))
            .andExpect(status().isBadRequest)
            .andExpect(view().name("error/404"))
    }
}
