package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

/**
 * MemberSettingsView 테스트
 */
class MemberSettingsViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `setting - success - returns setting view with member data`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(get(MemberUrls.SETTING))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SETTING))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - success - redirects with success message`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "UpdatedNickname")
                .param("profileAddress", "updatedProfile")
                .param("introduction", "Updated introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attribute("success", true))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - returns error when nickname is invalid`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "")
                .param("profileAddress", "updated-profile")
                .param("introduction", "Updated introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - returns error when nickname is too short`() {
        // Given
        testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "a")
                .param("profileAddress", "updated-profile")
                .param("introduction", "Updated introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `updateAccount - failure - returns error when member is not active`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "UpdatedNickname")
                .param("profileAddress", "updatedProfile")
                .param("introduction", "Updated introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - success - redirects with success message`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_PASSWORD_PLAIN)
                .param("newPassword", "NewPassword123!")
                .param("confirmNewPassword", "NewPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attribute("success", true))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - returns error when password format is invalid`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", "CurrentPassword123!")
                .param("newPassword", "weak")
                .param("confirmNewPassword", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - returns error when passwords do not match`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", "CurrentPassword123!")
                .param("newPassword", "NewPassword123!")
                .param("confirmNewPassword", "DifferentPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - success - redirects to home after account deletion`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - returns error when not confirmed`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - returns error when confirmation is null`() {
        // Given
        val member = testMemberHelper.getDefaultMember()

        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `deleteAccount - failure - returns error when member is not active`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#delete"))
            .andExpect(flash().attribute("error", "회원 탈퇴에 실패했습니다. 다시 시도해주세요."))
    }
}
