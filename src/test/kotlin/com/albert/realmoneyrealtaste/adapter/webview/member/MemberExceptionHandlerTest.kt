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
    fun `handleAlreadyActivated - AlreadyActivatedException returns activate view`() {
        val exception = AlreadyActivatedException("Already activated")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
    }

    @Test
    fun `handleAlreadyActivated - AlreadyActivatedException adds success false to model`() {
        val exception = AlreadyActivatedException("Already activated")

        handler.handleAlreadyActivated(exception, model)

        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - ExpiredActivationTokenException returns activate view`() {
        val exception = ExpiredActivationTokenException("Token expired")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
    }

    @Test
    fun `handleAlreadyActivated - ExpiredActivationTokenException adds success false to model`() {
        val exception = ExpiredActivationTokenException("Token expired")

        handler.handleAlreadyActivated(exception, model)

        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - InvalidActivationTokenException returns activate view`() {
        val exception = InvalidActivationTokenException("Invalid token")

        val viewName = handler.handleAlreadyActivated(exception, model)

        assertEquals(MemberView.MEMBER_ACTIVATE_VIEW_NAME, viewName)
    }

    @Test
    fun `handleAlreadyActivated - InvalidActivationTokenException adds success false to model`() {
        val exception = InvalidActivationTokenException("Invalid token")

        handler.handleAlreadyActivated(exception, model)

        verify { model.addAttribute("success", false) }
    }

    @Test
    fun `handleAlreadyActivated - all exceptions return same view`() {
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
    fun `handleAlreadyActivated - all exceptions add success false to model`() {
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
