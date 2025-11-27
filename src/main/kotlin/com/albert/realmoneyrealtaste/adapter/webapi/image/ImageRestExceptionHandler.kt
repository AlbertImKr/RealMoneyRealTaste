package com.albert.realmoneyrealtaste.adapter.webapi.image

import com.albert.realmoneyrealtaste.application.image.exception.ImageDeleteException
import com.albert.realmoneyrealtaste.application.image.exception.ImageGenerateException
import com.albert.realmoneyrealtaste.application.image.exception.ImageNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(annotations = [RestController::class])
class ImageRestExceptionHandler {

    @ExceptionHandler(ImageDeleteException::class)
    fun handleImageDeleteException(ex: ImageDeleteException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "이미지 삭제에 실패했습니다."
            )
        )
    }

    @ExceptionHandler(ImageNotFoundException::class)
    fun handleImageNotFoundException(ex: ImageNotFoundException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "이미지가 존재하지 않습니다."
            )
        )
    }

    @ExceptionHandler(ImageGenerateException::class)
    fun handleImageGenerateException(ex: ImageGenerateException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "이미지 생성에 실패했습니다."
            )
        )
    }
}
