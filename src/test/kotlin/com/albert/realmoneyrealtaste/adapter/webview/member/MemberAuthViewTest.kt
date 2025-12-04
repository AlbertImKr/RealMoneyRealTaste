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

/**
 * MemberAuthView 테스트
 */
class MemberAuthViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var activationTokenRepository: ActivationTokenRepository

    @Autowired
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Test
    fun `activate - success - returns activation view with nickname`() {
        // Given
        val member = testMemberHelper.createMember(email = "test@example.com")
        val token = createValidActivationToken(member.requireId())

        // When & Then
        mockMvc.perform(get(MemberUrls.ACTIVATION).param("token", token.token))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.ACTIVATE))
            .andExpect(model().attribute("nickname", member.nickname.value))
    }

    @Test
    fun `activate - failure - returns error when token is invalid`() {
        // When & Then
        mockMvc.perform(
            get(MemberUrls.ACTIVATION)
                .param("token", "invalid-token")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/activate"))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `activate - failure - returns error when token is expired`() {
        // Given
        val member = testMemberHelper.createMember(email = "test@example.com")
        val expiredToken = createExpiredActivationToken(member.requireId())

        // When & Then
        mockMvc.perform(
            get(MemberUrls.ACTIVATION)
                .param("token", expiredToken.token)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/activate"))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `activate - failure - returns error when token parameter is missing`() {
        // When & Then
        mockMvc.perform(get(MemberUrls.ACTIVATION))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivation GET - success - returns resend activation view with email`() {
        // When & Then
        mockMvc.perform(get(MemberUrls.RESEND_ACTIVATION))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.RESEND_ACTIVATION))
            .andExpect(model().attribute("email", MemberFixture.DEFAULT_EMAIL))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `resendActivationEmail - success - resends activation email for inactive member`() {
        // When & Then
        mockMvc.perform(post(MemberUrls.RESEND_ACTIVATION).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.RESEND_ACTIVATION))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail - failure - returns error when member is already active`() {
        // When & Then
        mockMvc.perform(post(MemberUrls.RESEND_ACTIVATION).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.RESEND_ACTIVATION))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `resendActivationEmail - failure - returns forbidden when not authenticated`() {
        // When & Then
        mockMvc.perform(post(MemberUrls.RESEND_ACTIVATION).with(csrf()))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `resendActivationEmail - failure - requires csrf token`() {
        // When & Then
        mockMvc.perform(post(MemberUrls.RESEND_ACTIVATION))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `passwordForgot GET - success - returns password forgot view`() {
        // When & Then
        mockMvc.perform(get(MemberUrls.PASSWORD_FORGOT))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PASSWORD_FORGOT))
    }

    @Test
    fun `sendPasswordResetEmail - success - sends reset email for existing member`() {
        // Given
        testMemberHelper.createActivatedMember(email = "test@example.com")

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", "test@example.com")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.PASSWORD_FORGOT))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `sendPasswordResetEmail - failure - validation error when email format is invalid`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", "invalid-email")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.PASSWORD_FORGOT))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `sendPasswordResetEmail - failure - validation error when email is empty`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", "")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.PASSWORD_FORGOT))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `sendPasswordResetEmail - failure - requires csrf token`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .param("email", "test@example.com")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `passwordReset GET - success - returns password reset view with token`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(get(MemberUrls.PASSWORD_RESET).param("token", token))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PASSWORD_RESET))
            .andExpect(model().attribute("token", token))
    }

    @Test
    fun `passwordReset GET - failure - returns error when token parameter is missing`() {
        // When & Then
        mockMvc.perform(get(MemberUrls.PASSWORD_RESET))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `resetPassword - success - resets password with valid token`() {
        // Given
        val member = testMemberHelper.createActivatedMember(email = "test@example.com")
        val token = createValidPasswordResetToken(member.requireId())

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token.token)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "NewPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `resetPassword - failure - validation error when passwords do not match`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "DifferentPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when password format is invalid`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "weak")
                .param("newPasswordConfirm", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when password is too short`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "Short1!")
                .param("newPasswordConfirm", "Short1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when password is too long`() {
        // Given
        val token = "valid-reset-token"
        val longPassword = "a".repeat(21) + "1!"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", longPassword)
                .param("newPasswordConfirm", longPassword)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when password confirmation is empty`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - returns error when token is invalid`() {
        // Given
        val invalidToken = "invalid-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", invalidToken)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "NewPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("success", false))
    }

    @Test
    fun `resetPassword - failure - returns error when token is expired`() {
        // Given
        val member = testMemberHelper.createActivatedMember(email = "test@example.com")
        val expiredToken = createExpiredPasswordResetToken(member.requireId())

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", expiredToken.token)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "NewPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("success", false))
    }

    @Test
    fun `resetPassword - failure - requires csrf token`() {
        // Given
        val token = "valid-reset-token"

        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .param("token", token)
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "NewPassword123!")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `resetPassword - failure - returns error when token parameter is missing`() {
        // When & Then
        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("newPassword", "NewPassword123!")
                .param("newPasswordConfirm", "NewPassword123!")
        )
            .andExpect(status().isBadRequest)
    }

    // ========== 헬퍼 메서드 ==========

    private fun createValidActivationToken(memberId: Long): ActivationToken {
        val token = ActivationToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(1)
        )
        return activationTokenRepository.save(token)
    }

    private fun createExpiredActivationToken(memberId: Long): ActivationToken {
        val token = ActivationToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now().minusDays(2),
            expiresAt = LocalDateTime.now().minusDays(1)
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

    private fun createExpiredPasswordResetToken(memberId: Long): PasswordResetToken {
        val token = PasswordResetToken(
            memberId = memberId,
            token = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now().minusHours(2),
            expiresAt = LocalDateTime.now().minusHours(1)
        )
        return passwordResetTokenRepository.save(token)
    }
}
