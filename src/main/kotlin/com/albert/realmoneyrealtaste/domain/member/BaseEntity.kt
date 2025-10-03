package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.proxy.HibernateProxy
import java.io.Serializable

@MappedSuperclass
abstract class BaseEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    final var id: Long? = null
        private set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        val oEffectiveClass = unwrapProxy(other)
        val thisEffectiveClass = unwrapProxy(this)
        if (thisEffectiveClass != oEffectiveClass) return false

        if (other !is BaseEntity) return false

        return id != null && id == other.id
    }

    private fun unwrapProxy(other: Any): Class<out Any>? =
        if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass

    override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}
