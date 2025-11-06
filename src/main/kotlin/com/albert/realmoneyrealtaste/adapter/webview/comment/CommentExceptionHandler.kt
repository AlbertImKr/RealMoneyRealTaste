package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreateRequestException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreationException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentNotFoundException
import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateRequestException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentStatusException
import com.albert.realmoneyrealtaste.domain.comment.exceptions.UnauthorizedCommentOperationException
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class CommentExceptionHandler {

    @ExceptionHandler(CommentNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCommentNotFound(
        ex: CommentNotFoundException,
        model: Model,
    ): String {
        model.addAttribute("error", "댓글을 찾을 수 없습니다.")
        return ERROR_VIEW_404
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

    @ExceptionHandler(UnauthorizedCommentOperationException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUnauthorizedOperation(
        ex: UnauthorizedCommentOperationException,
        model: Model,
    ): String {
        model.addAttribute("error", "댓글 권한이 없습니다.")
        return ERROR_VIEW_403
    }

    @ExceptionHandler(InvalidCommentStatusException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidStatus(
        ex: InvalidCommentStatusException,
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

    companion object {
        const val ERROR_VIEW_400 = "error/400"
        const val ERROR_VIEW_403 = "error/403"
        const val ERROR_VIEW_404 = "error/404"
    }
}
