package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.application.collection.exception.CollectionCreateException
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionDeleteException
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionNotFoundException
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CollectionExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CollectionUpdateException::class)
    fun handleCollectionUpdateException(ex: CollectionUpdateException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "컬렉션 정보 업데이트 중 오류가 발생했습니다: ${ex.message}",
            )
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CollectionCreateException::class)
    fun handleCollectionCreateException(ex: CollectionCreateException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "컬렉션 생성 중 오류가 발생했습니다: ${ex.message}",
            )
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CollectionNotFoundException::class)
    fun handleCollectionNotFoundException(ex: CollectionNotFoundException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "컬렉션을 찾을 수 없습니다: ${ex.message}",
            )
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CollectionDeleteException::class)
    fun handleCollectionDeleteException(ex: CollectionDeleteException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.badRequest().body(
            mapOf(
                "success" to false,
                "error" to "컬렉션 삭제 중 오류가 발생했습니다: ${ex.message}",
            )
        )
    }
}
