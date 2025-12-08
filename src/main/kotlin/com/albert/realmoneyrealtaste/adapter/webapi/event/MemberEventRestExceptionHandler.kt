package com.albert.realmoneyrealtaste.adapter.webapi.event

import com.albert.realmoneyrealtaste.application.event.exception.MemberEventNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(annotations = [RestController::class])
class MemberEventRestExceptionHandler {

    @ExceptionHandler
    fun handleMemberEventNotFoundException(e: MemberEventNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }
}
