package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.BeforeEach
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

class MemberViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberRegister: MemberRegister

    @Autowired
    private lateinit var activationTokenGenerator: ActivationTokenGenerator

    @Autowired
    private lateinit var activationTokenRepository: ActivationTokenRepository

    private val email = MemberFixture.DEFAULT_EMAIL
    private val password = MemberFixture.DEFAULT_RAW_PASSWORD
    private val nickname = MemberFixture.DEFAULT_NICKNAME
    private lateinit var member: Member

    @BeforeEach
    fun setup() {
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = nickname
        )
        member = memberRegister.register(request)
    }

    @Test
    fun `activate - success - returns success view with member information`() {
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")

        mockMvc.perform(
            get("/members/activate")
                .param("token", token.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("success", true))
            .andExpect(model().attribute("nickname", nickname.value))
    }

    @Test
    fun `activate - failure - returns failure view when token is invalid`() {
        val invalidToken = "invalid-token"

        mockMvc.perform(
            get("/members/activate")
                .param("token", invalidToken)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("success", false))
    }

    @Test
    fun `activate - failure - returns failure view when token is expired`() {
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")
        activationTokenRepository.delete(token)
        flushAndClear()
        val expiredToken = activationTokenGenerator.generate(member.id!!, -1)

        mockMvc.perform(
            get("/members/activate")
                .param("token", expiredToken.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("success", false))
    }

    @Test
    fun `activate - failure - returns failure view when member is already activated`() {
        val token = activationTokenRepository.findByMemberId(member.id!!)
            ?: throw IllegalStateException("Activation token not found for member id: ${member.id}")

        mockMvc.perform(
            get("/members/activate")
                .param("token", token.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("success", true))

        flushAndClear()
        val newToken = activationTokenGenerator.generate(member.id!!, 60 * 60)

        mockMvc.perform(
            get("/members/activate")
                .param("token", newToken.token)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_ACTIVATE_VIEW_NAME))
            .andExpect(model().attribute("success", true))
    }

    @Test
    fun `activate - failure - returns bad request when token parameter is missing`() {
        mockMvc.perform(
            get("/members/activate")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail GET - success - returns view with email`() {
        mockMvc.perform(
            get("/members/resend-activation")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_RESEND_ACTIVATION_VIEW_NAME))
            .andExpect(model().attribute("email", email))
    }

    @Test
    fun `resendActivationEmail GET - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            get("/members/resend-activation")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail POST - success - redirects with success message`() {
        mockMvc.perform(
            post("/members/resend-activation")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/resend-activation"))
            .andExpect(flash().attribute("success", true))
            .andExpect(flash().attribute("message", "인증 이메일이 재발송되었습니다. 이메일을 확인해주세요."))
    }

    @Test
    fun `resendActivationEmail POST - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/members/resend-activation")
                .with(csrf())
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `setting GET - success - returns setting view with member information`() {
        mockMvc.perform(
            get("/members/setting")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_SETTING_VIEW_NAME))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `setting GET - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            get("/members/setting")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - success - redirects with success message`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "newNickname")
                .param("profileAddress", "newAddress")
                .param("introduction", "new introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("success", "계정 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - failure - redirects with error when validation fails`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "1")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("error", "닉네임은 2자 이상 20자 이하로 입력해주세요."))
    }

    @Test
    fun `updateAccount POST - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "newNickname")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - success - redirects with success message`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "newPassword123!")
                .param("confirmNewPassword", "newPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("success", "비밀번호가 성공적으로 변경되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - failure - redirects with error when current password is incorrect`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .contentType("application/x-www-form-urlencoded")
                .param("currentPassword", "wrongPassword!1")
                .param("newPassword", "newPassword123!")
                .param("confirmNewPassword", "newPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("error", "현재 비밀번호가 일치하지 않습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - failure - redirects with error when validation fails`() {
        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .contentType("application/x-www-form-urlencoded")
                .param("currentPassword", "password123!")
                .param("newPassword", "short") // invalid password
                .param("confirmNewPassword", "short")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("error", "비밀번호 변경이 실패했습니다. 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"))
    }

    @Test
    fun `updatePassword POST - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .contentType("application/x-www-form-urlencoded")
                .param("currentPassword", "password123!")
                .param("newPassword", "newPassword123!")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount POST - success - redirects to signout when confirmed`() {
        member.activate()

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
    fun `deleteAccount POST - failure - redirects with error when not confirmed`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    fun `deleteAccount POST - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - failure - redirects with error when new passwords do not match`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "newPassword123!")
                .param("confirmNewPassword", "differentPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("error", "비밀번호 변경이 실패했습니다. 새 비밀번호와 비밀번호 확인이 일치하지 않습니다."))
            .andExpect(flash().attribute("tab", "password"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - failure - redirects with error when password format is invalid`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "weak")
                .param("confirmNewPassword", "weak")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("error", "비밀번호 변경이 실패했습니다. 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"))
            .andExpect(flash().attribute("tab", "password"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - includes tab attribute in flash when validation fails`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - includes tab attribute in flash on success`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "newNickname")
                .param("profileAddress", "newAddress")
                .param("introduction", "new introduction")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attribute("success", "계정 정보가 성공적으로 업데이트되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - failure - handles IllegalArgumentException from service`() {
        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "duplicatedNickname")
                .param("profileAddress", "address")
                .param("introduction", "intro")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attribute("error", "계정 정보 업데이트 중 오류가 발생했습니다. 등록 완료 상태에서만 정보 수정이 가능합니다"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount POST - includes tab attribute in flash when not confirmed`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "false")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount POST - includes tab attribute in flash when member is already deactivated`() {
        member.activate()
        member.deactivate()
        flushAndClear()

        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
                .param("confirmed", "true")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#delete"))
            .andExpect(flash().attribute("tab", "delete"))
            .andExpect(flash().attribute("error", "계정이 이미 비활성화되었거나 삭제할 수 없습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `deleteAccount POST - confirmed parameter is null - treats as false`() {
        mockMvc.perform(
            post("/members/setting/delete")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#delete"))
            .andExpect(flash().attribute("error", "계정 삭제 확인이 필요합니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - includes tab attribute in all error cases`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", "wrongPassword!1")
                .param("newPassword", "newPassword123!")
                .param("confirmNewPassword", "newPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePassword POST - includes tab attribute on success`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/password")
                .with(csrf())
                .param("currentPassword", MemberFixture.DEFAULT_RAW_PASSWORD.value)
                .param("newPassword", "newPassword123!")
                .param("confirmNewPassword", "newPassword123!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting#password"))
            .andExpect(flash().attribute("tab", "password"))
            .andExpect(flash().attribute("success", "비밀번호가 성공적으로 변경되었습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - handles very long nickname gracefully`() {
        val longNickname = "a".repeat(21) // exceeds 20 character limit

        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", longNickname)
                .param("profileAddress", "address")
                .param("introduction", "intro")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("tab", "account"))
            .andExpect(flash().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateAccount POST - allows updating with minimal required fields`() {
        member.activate()

        mockMvc.perform(
            post("/members/setting/account")
                .with(csrf())
                .param("nickname", "minimalNick")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/members/setting"))
            .andExpect(flash().attribute("tab", "account"))
    }
}
