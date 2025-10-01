package com.albert.realmoneyrealtaste.adapter.webview.auth

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.ui.Model
import kotlin.test.Test

class AuthExceptionHandlerTest {

    private val handler = AuthExceptionHandler()
    private val model: Model = mock()
    private val request: HttpServletRequest = mock()

    @Test
    fun `handleBadCredentials returns signin view`() {
        val exception = BadCredentialsException("Invalid credentials")
        `when`(request.getParameter("email")).thenReturn("test@example.com")

        val viewName = handler.handleBadCredentials(exception, model, request)

        assertEquals("auth/signin", viewName)
    }

    @Test
    fun `handleBadCredentials adds error message to model`() {
        val exception = BadCredentialsException("Invalid credentials")
        `when`(request.getParameter("email")).thenReturn("test@example.com")

        handler.handleBadCredentials(exception, model, request)

        verify(model).addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.")
    }

    @Test
    fun `handleBadCredentials adds signinForm with email to model`() {
        val email = "test@example.com"
        val exception = BadCredentialsException("Invalid credentials")
        `when`(request.getParameter("email")).thenReturn(email)

        handler.handleBadCredentials(exception, model, request)

        verify(model).addAttribute(eq("signinForm"), any<SigninForm>())
    }

    @Test
    fun `handleBadCredentials handles null email parameter`() {
        val exception = BadCredentialsException("Invalid credentials")
        `when`(request.getParameter("email")).thenReturn(null)

        val viewName = handler.handleBadCredentials(exception, model, request)

        assertEquals("auth/signin", viewName)
        verify(model).addAttribute(eq("signinForm"), any<SigninForm>())
    }

    @Test
    fun `handleBadCredentials creates signinForm with empty password`() {
        val email = "test@example.com"
        val exception = BadCredentialsException("Invalid credentials")
        `when`(request.getParameter("email")).thenReturn(email)

        handler.handleBadCredentials(exception, model, request)

        verify(model).addAttribute(eq("signinForm"), any<SigninForm>())
    }
}
