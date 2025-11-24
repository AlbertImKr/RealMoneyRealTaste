package com.albert.realmoneyrealtaste.domain.collection.value

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionOwnerTest {

    @Test
    fun `create - success - with valid member id`() {
        val owner = CollectionOwner(1L, "test")

        assertEquals(1L, owner.memberId)
        assertEquals("test", owner.nickname)
    }

    @Test
    fun `create - failure - with zero member id`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionOwner(0L, "test")
        }.let {
            assertEquals("소유자 회원 ID는 양수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - with negative member id`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionOwner(-1L, "test")
        }.let {
            assertEquals("소유자 회원 ID는 양수여야 합니다.", it.message)
        }
    }

    @Test
    fun `create - failure - with empty nickname`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionOwner(1L, "")
        }.let {
            assertEquals("소유자 닉네임은 비어있을 수 없습니다.", it.message)
        }
    }

    @Test
    fun `create - success - with valid nickname`() {
        val owner = CollectionOwner(1L, "테스트유저")

        assertEquals(1L, owner.memberId)
        assertEquals("테스트유저", owner.nickname)
    }

    @Test
    fun `create - success - with Korean nickname`() {
        val owner = CollectionOwner(1L, "맛집탐험가")

        assertEquals(1L, owner.memberId)
        assertEquals("맛집탐험가", owner.nickname)
    }

    @Test
    fun `create - success - with alphanumeric nickname`() {
        val owner = CollectionOwner(1L, "user123")

        assertEquals(1L, owner.memberId)
        assertEquals("user123", owner.nickname)
    }

    @Test
    fun `create - success - with large member id`() {
        val owner = CollectionOwner(Long.MAX_VALUE, "test")

        assertEquals(Long.MAX_VALUE, owner.memberId)
        assertEquals("test", owner.nickname)
    }
}
