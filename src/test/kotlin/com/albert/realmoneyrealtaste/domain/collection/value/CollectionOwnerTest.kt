package com.albert.realmoneyrealtaste.domain.collection.value

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionOwnerTest {

    @Test
    fun `create - success - with valid member id`() {
        val owner = CollectionOwner(1L)

        assertEquals(1L, owner.memberId)
    }

    @Test
    fun `create - failure - with zero member id`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionOwner(0L)
        }
    }

    @Test
    fun `create - failure - with negative member id`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionOwner(-1L)
        }
    }

    @Test
    fun `create - success - with large member id`() {
        val owner = CollectionOwner(Long.MAX_VALUE)

        assertEquals(Long.MAX_VALUE, owner.memberId)
    }
}
