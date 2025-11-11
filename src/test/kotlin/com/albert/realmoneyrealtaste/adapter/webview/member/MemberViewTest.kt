package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
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
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class MemberViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var activationTokenRepository: ActivationTokenRepository

    @Autowired
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Test
    fun `activate - success - activates member and shows success page`() {
        val member = testMemberHelper.createMember()
        val token = createValidActivationToken(member.requireId())

        mockMvc.perform(
            get("/members/activate")
                .param("token", token.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("nickname", member.nickname.value))
            .andExpect(model().attribute("success", true))
    }

    @Test
    fun `activate - failure - returns error when token is invalid`() {
        mockMvc.perform(
            get("/members/activate")
                .param("token", "invalid-token")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail GET - success - shows resend activation page`() {
        mockMvc.perform(get("/members/resend-activation"))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_RESEND_ACTIVATION_VIEW_NAME))
            .andExpect(model().attributeExists("email"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `resendActivationEmail POST - success - resends activation email`() {
        mockMvc.perform(
            post("/members/resend-activation")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/resend-activation"))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `resendActivationEmail - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/members/resend-activation")
                .with(csrf())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `setting - success - shows member setting page`() {
        mockMvc.perform(get("/members/setting"))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_SETTING_VIEW_NAME))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `setting - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(get("/members/setting"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - success - updates account info and redirects`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "새로운닉네임")
                .param("profileAddress", "newaddress")
                .param("introduction", "새로운 소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_SETTING_URL))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attribute("success", "계정 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - validation error when nickname is too short`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "a") // 너무 짧은 닉네임
                .param("profileAddress", "address")
                .param("introduction", "소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_SETTING_URL))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - validation error when nickname is too long`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "a".repeat(21)) // 너무 긴 닉네임
                .param("profileAddress", "address")
                .param("introduction", "소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_SETTING_URL))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - success - updates password and redirects`() {
        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_SETTING_URL}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attribute("success", "비밀번호가 성공적으로 변경되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - validation error when password format is invalid`() {
        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", "Default1!")
                .param("newPassword", "weak") // 약한 비밀번호
                .param("confirmNewPassword", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_SETTING_URL}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - validation error when passwords do not match`() {
        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", "Default1!")
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "DifferentPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_SETTING_URL}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attributeExists("error"))
    }

    // 계정 삭제 테스트
    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - success - deactivates member and redirects to home`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - returns error when confirmation is missing`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_SETTING_URL}#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - returns error when confirmed is null`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
            // confirmed 파라미터 없음
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_SETTING_URL}#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    // 비밀번호 찾기 테스트
    @Test
    fun `passwordForgot GET - success - shows password forgot page`() {
        mockMvc.perform(get(MemberView.MEMBER_PASSWORD_FORGOT_URL))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_PASSWORD_FORGOT_VIEW_NAME))
    }

    @Test
    fun `sendPasswordResetEmail - success - sends reset email and redirects`() {
        val member = testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_FORGOT_URL)
                .with(csrf())
                .param("email", member.email.address)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_PASSWORD_FORGOT_URL))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `sendPasswordResetEmail - failure - returns error when email format is invalid`() {
        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_FORGOT_URL)
                .with(csrf())
                .param("email", "invalid-email")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_PASSWORD_FORGOT_URL))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attribute("error", "올바른 이메일 형식을 입력해주세요."))
    }

    @Test
    fun `passwordReset GET - success - shows password reset page with token`() {
        val token = "valid-reset-token"

        mockMvc.perform(
            get(MemberView.MEMBER_PASSWORD_RESET_URL)
                .param("token", token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_PASSWORD_RESET_VIEW_NAME))
            .andExpect(model().attribute("token", token))
    }

    @Test
    fun `resetPassword - success - resets password and redirects to signin`() {
        val member = testMemberHelper.createActivatedMember()
        val token = createValidPasswordResetToken(member.requireId())

        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_RESET_URL)
                .with(csrf())
                .param("token", token.token)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/signin"))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `resetPassword - failure - validation error when password format is invalid`() {
        val token = "valid-token"

        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_RESET_URL)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "weak") // 약한 비밀번호
                .param("newPasswordConfirm", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_PASSWORD_RESET_URL}?token=$token"))
            .andExpect(flash().attribute("error", "비밀번호 형식이 올바르지 않습니다."))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when passwords do not match`() {
        val token = "valid-token"

        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_RESET_URL)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "DifferentPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberView.MEMBER_PASSWORD_RESET_URL}?token=$token"))
            .andExpect(flash().attribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - returns error when token is invalid`() {
        val invalidToken = "invalid-token"

        mockMvc.perform(
            post(MemberView.MEMBER_PASSWORD_RESET_URL)
                .with(csrf())
                .param("token", invalidToken)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_PASSWORD_FORGOT_URL))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", invalidToken))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - requires csrf token`() {
        mockMvc.perform(
            post("/members/setting/account")
                // .with(csrf()) 제거하여 CSRF 토큰 누락 상황 테스트
                .param("nickname", "새로운닉네임")
                .param("profileAddress", "newaddress")
                .param("introduction", "새로운 소개")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - requires csrf token`() {
        mockMvc.perform(
            post("/members/setting/password")
                // .with(csrf()) 제거
                .param("currentPassword", "Default1!")
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "NewPassword1!")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - requires csrf token`() {
        mockMvc.perform(
            post("/members/setting/delete")
                // .with(csrf()) 제거
                .param("confirmed", "true")
        )
            .andExpect(status().isForbidden)
    }

    private fun createValidActivationToken(memberId: Long): ActivationToken {
        val token = ActivationToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(1)
        )
        return activationTokenRepository.save(token)
    }

    private fun createValidPasswordResetToken(memberId: Long): PasswordResetToken {
        val token = PasswordResetToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusHours(1)
        )
        return passwordResetTokenRepository.save(token)
    }
}
