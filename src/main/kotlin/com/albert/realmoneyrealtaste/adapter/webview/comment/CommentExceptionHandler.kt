package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreateRequestException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreationException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateRequestException
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class CommentExceptionHandler {

    companion object {
        const val ERROR_VIEW_400 = "error/400"
        const val ERROR_VIEW_404 = "error/404"
    }

    @ExceptionHandler(
        CommentCreateRequestException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(
        ex: CommentCreateRequestException,
        model: Model,
    ): String {
        model.addAttribute("error", ex.message)
        return ERROR_VIEW_400
    }

    @ExceptionHandler(CommentUpdateRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUpdateValidationExceptions(
        ex: CommentUpdateRequestException,
        model: Model,
    ): String {
        model.addAttribute("error", ex.message)
        return ERROR_VIEW_400
    }

    @ExceptionHandler(CommentCreationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCommentCreation(
        ex: CommentCreationException,
        model: Model,
    ): String {
        model.addAttribute("error", "댓글 작성에 실패했습니다.")
        return ERROR_VIEW_400
    }

    @ExceptionHandler(CommentUpdateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCommentUpdate(
        ex: CommentUpdateException,
        model: Model,
    ): String {
        model.addAttribute("error", "댓글 수정에 실패했습니다.")
        return ERROR_VIEW_404
    }
}
