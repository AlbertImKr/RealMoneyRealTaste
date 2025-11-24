package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.application.follow.exception.UnfollowException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice(annotations = [Controller::class])
class FollowExceptionHandler {

    @ExceptionHandler(UnfollowException::class)
    fun handleUnfollowException(
        ex: UnfollowException,
        request: HttpServletRequest,
    ): ResponseEntity<String> {

        val targetId = request.getParameter("targetId")

        val followButtonHtml = """
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-octagon-fill"></i>
                <strong>오류!</strong>
                ${ex.message}
            </div>
            <button class="btn btn-primary rounded-circle icon-md ms-auto"
                    hx-delete="/members/$targetId/follow"
                    hx-target="#follow-button-$targetId"
                    hx-swap="innerHTML">
                <i class="bi bi-person-check-fill"></i>
            </button>
        """.trimIndent()

        return ResponseEntity.badRequest().body(followButtonHtml)
    }
}
