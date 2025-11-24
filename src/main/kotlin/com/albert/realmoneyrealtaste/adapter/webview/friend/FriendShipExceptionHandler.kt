package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.adapter.webview.comment.CommentExceptionHandler.Companion.ERROR_VIEW_400
import com.albert.realmoneyrealtaste.application.friend.exception.FriendResponseException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(annotations = [Controller::class])
class FriendShipExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FriendResponseException::class)
    fun handleFriendResponseException(
        ex: FriendResponseException,
        model: Model,
    ): String {
        model.addAttribute("error", ex.message)
        return ERROR_VIEW_400
    }
}
