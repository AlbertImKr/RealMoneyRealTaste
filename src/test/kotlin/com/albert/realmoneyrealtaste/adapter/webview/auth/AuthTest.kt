package com.albert.realmoneyrealtaste.adapter.webview.auth

import com.albert.realmoneyrealtaste.TestcontainersConfiguration
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
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
                .param("email", "test@example.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("nickname", "테스터")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
    }

    @Test
    fun `sign up - fail`() {
        mockMvc.perform(
            post("/signup")
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
}
