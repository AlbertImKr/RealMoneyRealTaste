package com.albert.realmoneyrealtaste.domain.member.value

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn

@Embeddable
class Roles(
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = COLLECTION_TABLE_NAME,
        joinColumns = [JoinColumn(name = JOIN_COLUMN_NAME)]
    )
    @Column(name = COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private val values: MutableSet<Role>,
) {

    fun getRoles(): Set<Role> = values.toSet()

    fun hasRole(role: Role): Boolean = values.contains(role)

    fun hasAnyRole(vararg roles: Role): Boolean = roles.any { values.contains(it) }

    fun addRole(role: Role) = values.add(role)

    fun removeRole(role: Role): Boolean {
        require(values.size > 1) { ERROR_EMPTY_ROLES }

        return values.remove(role)
    }

    fun isAdmin(): Boolean = hasRole(Role.ADMIN)

    fun isManager(): Boolean = hasRole(Role.MANAGER)

    companion object {
        const val COLLECTION_TABLE_NAME = "member_roles"
        const val JOIN_COLUMN_NAME = "member_id"
        const val COLUMN_NAME = "role"

        const val ERROR_EMPTY_ROLES = "적어도 하나의 역할이 필요합니다"

        fun ofUser(): Roles = Roles(mutableSetOf(Role.USER))

        fun of(vararg roles: Role): Roles {
            require(roles.isNotEmpty()) { ERROR_EMPTY_ROLES }

            return Roles(roles.toMutableSet())
        }
    }
}
