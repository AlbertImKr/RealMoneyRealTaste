package com.albert.realmoneyrealtaste.adapter.webview.collection

import com.albert.realmoneyrealtaste.application.collection.exception.CollectionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(annotations = [Controller::class])
class CollectionExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CollectionNotFoundException::class)
    fun handleCollectionNotFoundException(ex: CollectionNotFoundException, model: Model): String {
        model.addAttribute("success", false)
        model.addAttribute("error", "컬렉션을 찾을 수 없습니다.")
        return "error/400"
    }
}
