package com.albert.realmoneyrealtaste.domain.common

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
    var id: Long? = null
        protected set

    /**
     * 영속화된 회원의 ID를 반환합니다.
     *
     * @throws IllegalStateException 영속화되지 않은 회원인 경우
     */
    fun requireId(): Long {
        return id ?: throw IllegalStateException(
            "${this::class.simpleName}의 ID가 설정되지 않았습니다. 영속화된 엔티티에서만 ID를 조회할 수 있습니다.",
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        val oEffectiveClass = unwrapProxy(other)
        val thisEffectiveClass = unwrapProxy(this)
        if (thisEffectiveClass != oEffectiveClass) return false

        if (other !is BaseEntity) return false

        return id != null && id == other.id
    }

    private fun unwrapProxy(entity: Any): Class<out Any>? =
        if (entity is HibernateProxy) entity.hibernateLazyInitializer.persistentClass else entity.javaClass

    override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}
