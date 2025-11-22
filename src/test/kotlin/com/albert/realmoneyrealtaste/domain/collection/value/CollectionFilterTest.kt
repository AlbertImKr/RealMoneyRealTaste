package com.albert.realmoneyrealtaste.domain.collection.value

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CollectionFilterTest {

    @Test
    fun `from - success - returns ALL when value is null`() {
        val filter = CollectionFilter.from(null)

        assertEquals(CollectionFilter.ALL, filter)
    }

    @Test
    fun `from - success - returns ALL when value is all`() {
        val filter = CollectionFilter.from("all")
        
        assertEquals(CollectionFilter.ALL, filter)
    }

    @Test
    fun `from - success - returns PUBLIC when value is public`() {
        val filter = CollectionFilter.from("public")

        assertEquals(CollectionFilter.PUBLIC, filter)
    }

    @Test
    fun `from - success - returns ALL when value is invalid`() {
        val filter = CollectionFilter.from("invalid")

        assertEquals(CollectionFilter.ALL, filter)
    }

    @Test
    fun `from - success - returns ALL when value is empty string`() {
        val filter = CollectionFilter.from("")

        assertEquals(CollectionFilter.ALL, filter)
    }

    @Test
    fun `from - success - returns ALL when value is blank string`() {
        val filter = CollectionFilter.from("   ")

        assertEquals(CollectionFilter.ALL, filter)
    }

    @Test
    fun `from - success - case sensitive test`() {
        assertEquals(CollectionFilter.ALL, CollectionFilter.from("ALL")) // 대문자는 유효하지 않음
        assertEquals(CollectionFilter.ALL, CollectionFilter.from("All")) // 혼합 케이스는 유효하지 않음
        assertEquals(CollectionFilter.ALL, CollectionFilter.from("PUBLIC")) // 대문자는 유효하지 않음
        assertEquals(CollectionFilter.ALL, CollectionFilter.from("Public")) // 혼합 케이스는 유효하지 않음
    }

    @Test
    fun `values - success - returns correct string values`() {
        assertEquals("all", CollectionFilter.ALL.value)
        assertEquals("public", CollectionFilter.PUBLIC.value)
    }

    @Test
    fun `entries - success - contains all expected filters`() {
        val entries = CollectionFilter.entries

        assertEquals(2, entries.size)
        assertEquals(true, entries.contains(CollectionFilter.ALL))
        assertEquals(true, entries.contains(CollectionFilter.PUBLIC))
    }
}
