package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.application.member.required.PasswordResetTokenRepository
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.PasswordResetToken
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.hamcrest.Matchers
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
import kotlin.test.assertTrue

class MemberViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var activationTokenRepository: ActivationTokenRepository

    @Autowired
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository

    @Autowired
    private lateinit var friendshipRepository: FriendshipRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readProfile - success - shows profile with follow and friend status when viewing other member`() {
        val otherMember = testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )

        mockMvc.perform(
            get("/members/{id}", otherMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
            .andExpect(model().attributeExists("isFollowing"))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attribute("hasSentFriendRequest", false))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readProfile - success - shows profile with hasSentFriendRequest true when friend request sent`() {
        val currentUser = testMemberHelper.getDefaultMember()
        val otherMember = testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )

        // 친구 요청 생성
        val friendRequest = Friendship.request(
            FriendRequestCommand(
                fromMemberId = currentUser.requireId(),
                toMemberId = otherMember.requireId(),
                toMemberNickname = otherMember.nickname.value
            )
        )
        friendshipRepository.save(friendRequest)
        flushAndClear()

        mockMvc.perform(
            get("/members/{id}", otherMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
            .andExpect(model().attributeExists("isFollowing"))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attribute("hasSentFriendRequest", true))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readProfile - success - shows own profile without follow friend status`() {
        val member = testMemberHelper.getDefaultMember()

        mockMvc.perform(
            get("/members/{id}", member.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
    }

    @Test
    fun `readProfile - success - shows profile without authentication`() {
        val member = testMemberHelper.createActivatedMember(
            email = "public@example.com",
            nickname = "public"
        )

        mockMvc.perform(
            get("/members/{id}", member.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PROFILE))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("postCreateForm"))
    }

    @Test
    fun `readProfile - failure - returns error when member not found`() {
        mockMvc.perform(
            get("/members/{id}", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `activate - success - activates member and shows success page`() {
        val member = testMemberHelper.createMember()
        val token = createValidActivationToken(member.requireId())

        mockMvc.perform(
            get(MemberUrls.ACTIVATION)
                .param("token", token.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.ACTIVATE))
            .andExpect(model().attribute("nickname", member.nickname.value))
            .andExpect(model().attribute("success", true))
    }

    @Test
    fun `activate - failure - returns error when token is invalid`() {
        mockMvc.perform(
            get(MemberUrls.ACTIVATION)
                .param("token", "invalid-token")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail GET - success - shows resend activation page`() {
        mockMvc.perform(get(MemberUrls.RESEND_ACTIVATION))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.RESEND_ACTIVATION))
            .andExpect(model().attributeExists("email"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `resendActivationEmail POST - success - resends activation email`() {
        mockMvc.perform(
            post(MemberUrls.RESEND_ACTIVATION)
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.RESEND_ACTIVATION))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail - failure - returns error when member is already active`() {
        mockMvc.perform(
            post(MemberUrls.RESEND_ACTIVATION)
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.RESEND_ACTIVATION))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `resendActivationEmail - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post(MemberUrls.RESEND_ACTIVATION)
                .with(csrf())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `setting - success - shows member setting page`() {
        mockMvc.perform(get(MemberUrls.SETTING))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SETTING))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `setting - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(get(MemberUrls.SETTING))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - success - updates account info and redirects`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "새로운닉네임")
                .param("profileAddress", "newaddress")
                .param("introduction", "새로운 소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attribute("success", "계정 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - validation error when nickname is too short`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "a") // 너무 짧은 닉네임
                .param("profileAddress", "address")
                .param("introduction", "소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - validation error when nickname is too long`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "a".repeat(21)) // 너무 긴 닉네임
                .param("profileAddress", "address")
                .param("introduction", "소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - profile address is duplicated`() {
        val duplicateProfileAddress = "existingaddress"
        val otherUser = testMemberHelper.createActivatedMember(email = "other@user.com")
        otherUser.updateInfo(
            profileAddress = ProfileAddress(duplicateProfileAddress),
        )
        flushAndClear()

        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "새로운닉네임")
                .param("profileAddress", duplicateProfileAddress) // 중복된 프로필 주소
                .param("introduction", "새로운 소개")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.SETTING + "#account"))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `updateAccount - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
                .with(csrf())
                .param("nickname", "새로운닉네임")
                .param("profileAddress", "newaddress")
                .param("introduction", "새로운 소개")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - success - updates password and redirects`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attribute("success", "비밀번호가 성공적으로 변경되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - validation error when password format is invalid`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", "Default1!")
                .param("newPassword", "weak") // 약한 비밀번호
                .param("confirmNewPassword", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword - failure - validation error when passwords do not match`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", "Default1!")
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "DifferentPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `updatePassword - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_PASSWORD)
                .with(csrf())
                .param("currentPassword", "Default1!")
                .param("newPassword", "NewPassword1!")
                .param("confirmNewPassword", "NewPassword1!")
        )
            .andExpect(status().isForbidden)
    }

    // 계정 삭제 테스트
    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - success - deactivates member and redirects to home`() {
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
    fun `deleteAccount - failure - returns error when confirmation is missing`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount - failure - returns error when confirmed is null`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
            // confirmed 파라미터 없음
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.SETTING}#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME, active = false)
    fun `deleteAccount - failure - returns error when member is not active`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#delete"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    fun `deleteAccount - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_DELETE)
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().isForbidden)
    }

    // 비밀번호 찾기 테스트
    @Test
    fun `passwordForgot GET - success - shows password forgot page`() {
        mockMvc.perform(get(MemberUrls.PASSWORD_FORGOT))
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PASSWORD_FORGOT))
    }

    @Test
    fun `sendPasswordResetEmail - success - sends reset email and redirects`() {
        val member = testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", member.email.address)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.PASSWORD_FORGOT))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `sendPasswordResetEmail - failure - returns error when email format is invalid`() {
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", "invalid-email")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberUrls.PASSWORD_FORGOT))
            .andExpect(flash().attribute("success", false))
            .andExpect(flash().attribute("error", "올바른 이메일 형식을 입력해주세요."))
    }

    @Test
    fun `sendPasswordResetEmail - failure - returns error when email does not exist`() {
        mockMvc.perform(
            post(MemberUrls.PASSWORD_FORGOT)
                .with(csrf())
                .param("email", "nonexistent@example.com")
        )
            .andExpect(status().isBadRequest)
            .andExpect { view().name("error/404") }
    }

    @Test
    fun `passwordReset GET - success - shows password reset page with token`() {
        val token = "valid-reset-token"

        mockMvc.perform(
            get(MemberUrls.PASSWORD_RESET)
                .param("token", token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.PASSWORD_RESET))
            .andExpect(model().attribute("token", token))
    }

    @Test
    fun `resetPassword - success - resets password and redirects to signin`() {
        val member = testMemberHelper.createActivatedMember()
        val token = createValidPasswordResetToken(member.requireId())

        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token.token)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attributeExists("message"))
    }

    @Test
    fun `resetPassword - failure - validation error when password format is invalid`() {
        val token = "valid-token"

        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "weak") // 약한 비밀번호
                .param("newPasswordConfirm", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attribute("error", "비밀번호 형식이 올바르지 않습니다."))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - validation error when passwords do not match`() {
        val token = "valid-token"

        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", token)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "DifferentPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("${MemberUrls.PASSWORD_RESET}?token=$token"))
            .andExpect(flash().attribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."))
            .andExpect(flash().attribute("token", token))
    }

    @Test
    fun `resetPassword - failure - returns error when token is invalid`() {
        val invalidToken = "invalid-token"

        mockMvc.perform(
            post(MemberUrls.PASSWORD_RESET)
                .with(csrf())
                .param("token", invalidToken)
                .param("newPassword", "NewPassword1!")
                .param("newPasswordConfirm", "NewPassword1!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("error"))
            .andExpect(flash().attribute("success", false))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount - failure - requires csrf token`() {
        mockMvc.perform(
            post(MemberUrls.SETTING_ACCOUNT)
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
            post(MemberUrls.SETTING_PASSWORD)
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
            post(MemberUrls.SETTING_DELETE)
                // .with(csrf()) 제거
                .param("confirmed", "true")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readSidebarFragment - success - returns suggested users with follow status`() {
        mockMvc.perform(
            get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT))
            .andExpect(model().attributeExists("suggestedUsers"))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readSidebarFragment - success - returns multiple suggested users when other users exist`() {
        // 여러 다른 사용자 생성
        val otherUsers = (1..10).map { index ->
            testMemberHelper.createActivatedMember(
                email = "user$index@example.com",
                nickname = "user$index"
            )
        }

        mockMvc.perform(
            get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT))
            .andExpect(model().attributeExists("suggestedUsers"))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attribute("member", Matchers.notNullValue()))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readSidebarFragment - success - returns suggested users when many users exist`() {
        // 더 많은 사용자 생성하여 추천 알고리즘 테스트
        val otherUsers = (1..20).map { index ->
            testMemberHelper.createActivatedMember(
                email = "suggestuser$index@example.com",
                nickname = "suggest$index"
            )
        }
        flushAndClear()

        val result = mockMvc.perform(
            get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT))
            .andExpect(model().attribute("suggestedUsers", Matchers.hasSize<Member>(5)))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andReturn()
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readSidebarFragment - success - returns empty suggested users when no other users exist`() {
        val result = mockMvc.perform(
            get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberViews.SUGGEST_USERS_SIDEBAR_CONTENT))
            .andExpect(model().attributeExists("suggestedUsers"))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andReturn()

        // suggestedUsers가 비어있는지 확인
        val suggestedUsers = result.modelAndView!!.model["suggestedUsers"] as List<*>
        assertTrue(suggestedUsers.isEmpty())
    }

    @Test
    fun `readSidebarFragment - success - returns empty fragment without authentication`() {
        mockMvc.perform(
            get(MemberUrls.FRAGMENT_SUGGEST_USERS_SIDEBAR)
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
