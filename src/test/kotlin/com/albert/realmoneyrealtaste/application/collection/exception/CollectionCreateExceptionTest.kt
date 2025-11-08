package com.albert.realmoneyrealtaste.application.collection.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CollectionCreateExceptionTest {

    @Test
    fun `CollectionCreateException - success - creates exception with message only`() {
        val message = "컬렉션 생성 실패"
        val exception = CollectionCreateException(message)

        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `CollectionCreateException - success - creates exception with message and cause`() {
        val message = "컬렉션 생성 실패"
        val cause = IllegalArgumentException("잘못된 파라미터")
        val exception = CollectionCreateException(message, cause)

        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `CollectionCreateException - success - preserves stack trace when wrapping cause`() {
        val originalException = IllegalStateException("원본 예외")
        val wrappedException = CollectionCreateException("래핑된 예외", originalException)

        assertEquals("래핑된 예외", wrappedException.message)
        assertEquals(originalException, wrappedException.cause)
        assertEquals("원본 예외", wrappedException.cause?.message)
    }

    @Test
    fun `CollectionCreateException - success - handles null cause gracefully`() {
        val exception = CollectionCreateException("메시지", null)

        assertEquals("메시지", exception.message)
        assertNull(exception.cause)
    }
}
