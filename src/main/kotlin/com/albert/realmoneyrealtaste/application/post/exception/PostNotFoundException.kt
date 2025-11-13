package com.albert.realmoneyrealtaste.application.post.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class PostNotFoundException(message: String, cause: Throwable) : PostApplicationException(message, cause)
