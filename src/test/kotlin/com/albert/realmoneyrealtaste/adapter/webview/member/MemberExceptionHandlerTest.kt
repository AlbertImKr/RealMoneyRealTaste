package com.albert.realmoneyrealtaste.adapter.webview.member

import com.albert.realmoneyrealtaste.application.member.exception.AlreadyActivatedException
import com.albert.realmoneyrealtaste.application.member.exception.ExpiredActivationTokenException
import com.albert.realmoneyrealtaste.application.member.exception.InvalidActivationTokenException
import io.mockk.mockk
import io.mockk.verify
import org.springframework.ui.Model
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberExceptionHandlerTest {

    private val handler = MemberExceptionHandler()
    private val model: Model = mockk(relaxed = true)

    @Test
    fun `handleActivationTokenExceptions - success - returns activate view for ExpiredActivationTokenException`() {
        val exception = ExpiredActivationTokenException("Token expired")

        val viewName = handler.handleActivationTokenExceptions(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleActivationTokenExceptions - success - returns activate view for InvalidActivationTokenException`() {
        val exception = InvalidActivationTokenException("Invalid token")

        val viewName = handler.handleActivationTokenExceptions(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleActivationTokenExceptions - success - adds success false to model`() {
        val exceptions = listOf(
            ExpiredActivationTokenException("Token expired"),
            InvalidActivationTokenException("Invalid token")
        )

        exceptions.forEach { exception ->
            handler.handleActivationTokenExceptions(exception, model)
        }

        verify(exactly = 2) { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - success - returns activate view`() {
        val exception = AlreadyActivatedException("Already activated")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
    }

    @Test
    fun `handleAlreadyActivated - success - adds success true and message to model`() {
        val exception = AlreadyActivatedException("Already activated")

        handler.handleAlreadyActivated(exception, model)

        verify { model.addAttribute("success", true) }
        verify { model.addAttribute("message", "이미 활성화된 회원입니다.") }
    }
}
