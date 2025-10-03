package com.albert.realmoneyrealtaste.domain.member

import org.hibernate.proxy.HibernateProxy
import org.hibernate.proxy.LazyInitializer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BaseEntityTest {

    @Test
    fun `id is null when entity is newly created`() {
        val entity = TestBaseEntity()

        assertEquals(null, entity.id)
    }

    @Test
    fun `id can be set via reflection for testing purposes`() {
        val entity = TestBaseEntity()
        MemberFixture.setId(entity, 42L)

        assertEquals(42L, entity.id)
    }

    @Test
    fun `equals returns true for same object`() {
        val entity = TestBaseEntity()
        MemberFixture.setId(entity, 1L)

        assertEquals(entity, entity)
    }

    @Test
    fun `equals returns false for null object`() {
        val entity = TestBaseEntity()
        MemberFixture.setId(entity, 1L)

        assertEquals(false, entity.equals(null))
    }

    @Test
    fun `equals returns true for same class and id`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }

        assertEquals(entity1, entity2)
    }

    @Test
    fun `equals returns false for same class but different id`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1, entity2)
    }

    @Test
    fun `equals returns false when id is null`() {
        val entity1 = TestBaseEntity()
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1, entity2)
    }

    @Test
    fun `equals returns true for HibernateProxy with same class and id`() {
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val proxy = TestHibernateProxy().apply { MemberFixture.setId(this, 1L) }

        assertEquals(true, proxy.equals(entity))
        assertEquals(true, entity.equals(proxy))
    }

    @Test
    fun `equals returns false for HibernateProxy with different class`() {
        val proxy = TestHibernateProxy().apply { MemberFixture.setId(this, 1L) }
        val other = AnotherBaseEntity().apply { MemberFixture.setId(this, 1L) }

        assertEquals(false, proxy.equals(other))
        assertEquals(false, other.equals(proxy))
    }

    @Test
    fun `equals returns false for different classes`() {
        val entity1 = TestHibernateProxy()
        val entity2 = AnotherHibernateProxy()

        assertEquals(false, entity1 == entity2)
    }

    @Test
    fun `hashCode is consistent for same class`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = TestBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun `hashCode differs for different classes`() {
        val entity1 = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val entity2 = AnotherBaseEntity().apply { MemberFixture.setId(this, 2L) }

        assertNotEquals(entity1.hashCode(), entity2.hashCode())
    }

    @Test
    fun `hashCode matches for HibernateProxy`() {
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val proxy = TestHibernateProxy()

        assertEquals(proxy.hashCode(), entity.hashCode())
    }

    @Test
    fun `toString includes class name and id`() {
        val entity = TestBaseEntity().apply { MemberFixture.setId(this, 1L) }
        val toString = entity.toString()

        assertEquals(true, toString.contains("id = 1"))
        assertEquals(true, toString.contains("TestBaseEntity"))
    }

    internal class TestBaseEntity : BaseEntity()

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
