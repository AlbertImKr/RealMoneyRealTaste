package com.albert.realmoneyrealtaste.adapter.webapi.collection

import com.albert.realmoneyrealtaste.application.collection.exception.CollectionCreateException
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionNotFoundException
import com.albert.realmoneyrealtaste.application.collection.exception.CollectionUpdateException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionExceptionHandlerTest {

    private val exceptionHandler = CollectionExceptionHandler()

    @Test
    fun `handleCollectionUpdateException - returns 400 with error message`() {
        val errorMessage = "컬렉션 업데이트 실패"
        val exception = CollectionUpdateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionUpdateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionCreateException - returns 400 with error message`() {
        val errorMessage = "컬렉션 생성 실패"
        val exception = CollectionCreateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionCreateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션 생성 중 오류가 발생했습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionNotFoundException - returns 400 with error message`() {
        val errorMessage = "컬렉션을 찾을 수 없음"
        val exception = CollectionNotFoundException(errorMessage)

        val response = exceptionHandler.handleCollectionNotFoundException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션을 찾을 수 없습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionUpdateException - handles empty message gracefully`() {
        val errorMessage = "컬렉션 업데이트 실패"
        val exception = CollectionUpdateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionUpdateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션 정보 업데이트 중 오류가 발생했습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionCreateException - handles empty message gracefully`() {
        val errorMessage = "컬렉션이 존재하지 않습니다."
        val exception = CollectionCreateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionCreateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션 생성 중 오류가 발생했습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionNotFoundException - handles empty message gracefully`() {
        val errorMessage = "컬렉션이 존재하지 않습니다."
        val exception = CollectionNotFoundException(errorMessage)

        val response = exceptionHandler.handleCollectionNotFoundException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        assertFalse(body["success"] as Boolean)
        assertEquals("컬렉션을 찾을 수 없습니다: $errorMessage", body["error"] as String)
    }

    @Test
    fun `handleCollectionUpdateException - preserves original error message formatting`() {
        val errorMessage = "권한이 없습니다: 컬렉션 소유자만 삭제할 수 있습니다"
        val exception = CollectionUpdateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionUpdateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        val errorText = body["error"] as String
        assertTrue(errorText.contains("컬렉션 정보 업데이트 중 오류가 발생했습니다:"))
        assertTrue(errorText.contains(errorMessage))
    }

    @Test
    fun `handleCollectionCreateException - preserves original error message formatting`() {
        val errorMessage = "이름이 중복됩니다: 동일한 이름의 컬렉션이 이미 존재합니다"
        val exception = CollectionCreateException(errorMessage, IllegalArgumentException())

        val response = exceptionHandler.handleCollectionCreateException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        val errorText = body["error"] as String
        assertTrue(errorText.contains("컬렉션 생성 중 오류가 발생했습니다:"))
        assertTrue(errorText.contains(errorMessage))
    }

    @Test
    fun `handleCollectionNotFoundException - preserves original error message formatting`() {
        val errorMessage = "ID 1234에 해당하는 컬렉션을 찾을 수 없습니다"
        val exception = CollectionNotFoundException(errorMessage)

        val response = exceptionHandler.handleCollectionNotFoundException(exception)

        assertEquals(400, response.statusCode.value())
        val body = response.body as Map<String, Any>
        val errorText = body["error"] as String
        assertTrue(errorText.contains("컬렉션을 찾을 수 없습니다:"))
        assertTrue(errorText.contains(errorMessage))
    }
}
