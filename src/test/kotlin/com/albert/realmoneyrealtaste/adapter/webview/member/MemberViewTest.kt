package com.albert.realmoneyrealtaste.adapter.webview.member

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
class MemberViewTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `register form display`() {
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk)
            .andExpect(view().name("member/signup"))
            .andExpect(model().attributeExists("memberRegisterForm"))
    }

    @Test
    fun `register member - success`() {
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
    fun `register member - fail`() {
        mockMvc.perform(
            post("/signup")
                .param("email", "invalid-email")
                .param("password", "short")
                .param("confirmPassword", "different")
                .param("nickname", "A")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("member/signup"))
            .andExpect(model().attributeHasFieldErrors("memberRegisterForm", "email"))
            .andExpect(model().attributeHasFieldErrors("memberRegisterForm", "password"))
            .andExpect(model().attributeHasFieldErrors("memberRegisterForm", "confirmPassword"))
            .andExpect(model().attributeHasFieldErrors("memberRegisterForm", "nickname"))
    }
}
