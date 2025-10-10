package com.albert.realmoneyrealtaste.domain.member

import org.hibernate.proxy.HibernateProxy
import org.hibernate.proxy.LazyInitializer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class BaseEntityTest {

    @Test
    fun `id - success - returns null when entity is newly created`() {
        val entity = TestBaseEntity()

        assertEquals(null, entity.id)
    }

    @Test
    fun `id - success - can be set via reflection for testing purposes`() {
        val entity = TestBaseEntity()
        val expectedId = 42L

        MemberFixture.setId(entity, expectedId)

        assertEquals(expectedId, entity.id)
    }

    @Test
    fun `setIdForTest - success - sets id through protected setter`() {
        val entity = TestBaseEntity()
        val expectedId = 10L

        assertEquals(null, entity.id)

        entity.setIdForTest(expectedId)

        assertEquals(expectedId, entity.id)
    }

    @Test
    fun `equals - success - returns true for same object`() {
        val entity = TestBaseEntity()
        MemberFixture.setId(entity, 1L)

        assertEquals(entity, entity)
    }

    @Test
    fun `equals - failure - returns false for null object`() {
        val entity = TestBaseEntity()
        MemberFixture.setId(entity, 1L)

        assertEquals(false, entity.equals(null))
    }

    @Test
    fun `equals - success - returns true for same class and id`() {
        val id = 1L
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, id) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, id) }

        assertEquals(entity1, entity2)
    }

    @Test
    fun `equals - failure - returns false for same class but different id`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1, entity2)
    }

    @Test
    fun `equals - failure - returns false when id is null`() {
        val entity1 = TestBaseEntity()
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1, entity2)
    }

    @Test
    fun `equals - success - returns true for HibernateProxy with same class and id`() {
        val id = 1L
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, id) }
        val proxy = TestHibernateProxy().apply { MemberFixture.setId(this, id) }

        assertEquals(true, proxy.equals(entity))
        assertEquals(true, entity.equals(proxy))
    }

    @Test
    fun `equals - failure - returns false for HibernateProxy with different class`() {
        val id = 1L
        val proxy = TestHibernateProxy().apply { MemberFixture.setId(this, id) }
        val other = AnotherBaseEntity().apply { MemberFixture.setId(this, id) }

        assertEquals(false, proxy.equals(other))
        assertEquals(false, other.equals(proxy))
    }

    @Test
    fun `equals - failure - returns false for different classes`() {
        val entity1 = TestHibernateProxy()
        val entity2 = AnotherHibernateProxy()

        assertEquals(false, entity1 == entity2)
    }

    @Test
    fun `hashCode - success - is consistent for same class`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun `hashCode - success - differs for different classes`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = AnotherBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun `hashCode - success - matches for HibernateProxy`() {
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val proxy = TestHibernateProxy()

        assertEquals(proxy.hashCode(), entity.hashCode())
    }

    @Test
    fun `toString - success - includes class name and id`() {
        val id = 1L
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, id) }

        val toString = entity.toString()

        assertEquals(true, toString.contains("id = $id"))
        assertEquals(true, toString.contains("TestBaseEntity"))
    }

    @Test
    fun `requireId - success - returns id when entity is persisted`() {
        val expectedId = 100L
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, expectedId) }

        val id = entity.requireId()

        assertEquals(expectedId, id)
    }

    @Test
    fun `requireId - failure - throws exception when entity is not persisted`() {
        val entity = TestBaseEntity()

        assertFailsWith<IllegalStateException> {
            entity.requireId()
        }.also {
            assertEquals("영속화되지 않은 회원입니다", it.message)
        }
    }

    internal class TestBaseEntity : BaseEntity() {
        fun setIdForTest(newId: Long?) {
            this.id = newId
        }
    }

    internal class AnotherBaseEntity : BaseEntity()

    internal class TestHibernateProxy : BaseEntity(), HibernateProxy {
        private val lazyInitializer = mock(LazyInitializer::class.java).apply {
            `when`(persistentClass).thenReturn(TestBaseEntity::class.java)
        }

        override fun getHibernateLazyInitializer(): LazyInitializer = lazyInitializer
        override fun writeReplace(): Any = this
    }

    internal class AnotherHibernateProxy : HibernateProxy {
        private val lazyInitializer = mock(LazyInitializer::class.java).apply {
            `when`(persistentClass).thenReturn(TestBaseEntity::class.java)
        }

        override fun getHibernateLazyInitializer(): LazyInitializer = lazyInitializer
        override fun writeReplace(): Any = this
    }
}
