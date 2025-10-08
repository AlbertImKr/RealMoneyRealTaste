package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.required.ActivationTokenRepository
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
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
    @WithMockUser(username = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail GET - success - returns view with email`() {
        mockMvc.perform(
            get("/members/resend-activation")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(MemberView.MEMBER_RESEND_ACTIVATION_VIEW_NAME))
            .andExpect(model().attribute("email", email.address))
    }

    @Test
    fun `resendActivationEmail GET - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            get("/members/resend-activation")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = MemberFixture.DEFAULT_USERNAME)
    fun `resendActivationEmail POST - success - redirects with success message`() {
        mockMvc.perform(
            post("/members/resend-activation")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(MemberView.MEMBER_RESEND_ACTIVATION_VIEW_NAME))
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
}
