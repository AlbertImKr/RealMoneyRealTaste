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
    fun `handleAlreadyActivated - success - returns activate view for AlreadyActivatedException`() {
        val exception = AlreadyActivatedException("Already activated")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - success - returns activate view for ExpiredActivationTokenException`() {
        val exception = ExpiredActivationTokenException("Token expired")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - success - returns activate view for InvalidActivationTokenException`() {
        val exception = InvalidActivationTokenException("Invalid token")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - success - returns same view for all exception types`() {
        val exceptions = listOf(
            AlreadyActivatedException("Already activated"),
            ExpiredActivationTokenException("Token expired"),
            InvalidActivationTokenException("Invalid token")
        )

        exceptions.forEach { exception ->
            val viewName = handler.handleAlreadyActivated(exception, model)
            assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
        }
    }

    @Test
    fun `handleAlreadyActivated - success - adds success false to model for all exception types`() {
        val exceptions = listOf(
            AlreadyActivatedException("Already activated"),
            ExpiredActivationTokenException("Token expired"),
            InvalidActivationTokenException("Invalid token")
        )

        exceptions.forEach { exception ->
            handler.handleAlreadyActivated(exception, model)
        }

        verify(exactly = 3) { model.addAttribute("success", false) }
    }
}
