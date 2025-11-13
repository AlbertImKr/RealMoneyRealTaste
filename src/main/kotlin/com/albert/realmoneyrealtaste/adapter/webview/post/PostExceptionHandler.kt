package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.application.post.exception.PostCreateException
import com.albert.realmoneyrealtaste.application.post.exception.PostUpdateException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.net.URI

@ControllerAdvice(annotations = [Controller::class])
class PostExceptionHandler {

    @ExceptionHandler(PostUpdateException::class)
    fun handlePostUpdateException(
        ex: PostUpdateException,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "게시물 수정에 실패했습니다. 다시 시도해주세요.")

        val currentUrl = URI.create(request.requestURL.toString()).toURL().path
        return "redirect:$currentUrl"
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PostCreateException::class)
    fun handlePostCreateException(
        ex: PostCreateException,
        redirectAttributes: RedirectAttributes,
    ): String {
        redirectAttributes.addFlashAttribute("success", false)
        redirectAttributes.addFlashAttribute("error", "게시물 작성에 실패했습니다. 다시 시도해주세요.")

        return "redirect:${PostUrls.CREATE}"
    }
}
