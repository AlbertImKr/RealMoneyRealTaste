package com.albert.realmoneyrealtaste.adapter.webview.auth

import com.albert.realmoneyrealtaste.TestcontainersConfiguration
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegister
import com.albert.realmoneyrealtaste.application.member.provided.MemberRegisterRequest
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration::class)
class AuthTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberRegister: MemberRegister

    @Test
    fun `signup form display`() {
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk)
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeExists("signupForm"))
    }

    @Test
    fun `sign up - success`() {
        mockMvc.perform(
            post("/signup")
                .with(csrf())
                .param("email", "test@example.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("nickname", "테스터")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/signin"))
    }

    @Test
    fun `sign up - fail`() {
        mockMvc.perform(
            post("/signup")
                .with(csrf())
                .param("email", "invalid-email")
                .param("password", "short")
                .param("confirmPassword", "different")
                .param("nickname", "A")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "email"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "password"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "confirmPassword"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "nickname"))
    }

    @Nested
    inner class Signin {

        var password = MemberFixture.DEFAULT_RAW_PASSWORD
        var email = MemberFixture.DEFAULT_EMAIL

        @BeforeEach
        fun setup() {
            val request = MemberRegisterRequest(
                email = email,
                password = password,
                nickname = MemberFixture.DEFAULT_NICKNAME,
            )

            memberRegister.register(request)
        }

        @Test
        fun `signin form display`() {
            mockMvc.perform(get("/signin"))
                .andExpect(status().isOk)
                .andExpect(view().name("auth/signin"))
                .andExpect(model().attributeExists("signinForm"))
        }

        @Test
        fun `sign in - success`() {
            mockMvc.perform(
                post("/signin")
                    .with(csrf())
                    .param("email", email.address)
                    .param("password", password.value)
            )
                .andExpect(status().is3xxRedirection)
                .andExpect(redirectedUrl("/"))
        }

        @Test
        fun `sign in - fail with invalid email format`() {
            mockMvc.perform(
                post("/signin")
                    .with(csrf())
                    .param("email", "invalid-email")
                    .param("password", password.value)
            )
                .andExpect(status().isOk)
                .andExpect(view().name("auth/signin"))
                .andExpect(model().attributeHasFieldErrors("signinForm", "email"))
        }

        @Test
        fun `sign in - fail with empty password`() {
            mockMvc.perform(
                post("/signin")
                    .with(csrf())
                    .param("email", email.address)
                    .param("password", "")
            )
                .andExpect(status().isOk)
                .andExpect(view().name("auth/signin"))
                .andExpect(model().attributeHasFieldErrors("signinForm", "password"))
        }

        @Test
        fun `sign in - fail with wrong credentials`() {
            mockMvc.perform(
                post("/signin")
                    .with(csrf())
                    .param("email", email.address)
                    .param("password", "WrongPassword123!")
            )
                .andExpect(status().isOk)
                .andExpect(view().name("auth/signin"))
        }

        @Test
        fun `sign in - fail with non-existent user`() {
            mockMvc.perform(
                post("/signin")
                    .with(csrf())
                    .param("email", "nonexistent@example.com")
                    .param("password", "Password123!")
            )
                .andExpect(status().isOk)
                .andExpect(view().name("auth/signin"))
        }
    }
}
