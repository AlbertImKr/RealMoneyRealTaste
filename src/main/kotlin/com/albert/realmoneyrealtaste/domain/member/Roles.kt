package com.albert.realmoneyrealtaste.domain.member

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
        name = "member_roles",
        joinColumns = [JoinColumn(name = "member_id")]
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private val values: MutableSet<Role>,
) {

    fun getRoles(): Set<Role> = values.toSet()

    fun hasRole(role: Role): Boolean = values.contains(role)

    fun hasAnyRole(vararg roles: Role): Boolean = roles.any { values.contains(it) }

    fun addRole(role: Role) {
        values.add(role)
    }

    fun removeRole(role: Role) {
        require(values.size > 1 || role != Role.USER) {
            "최소 하나의 역할은 유지되어야 합니다"
        }
        values.remove(role)
    }

    fun isAdmin(): Boolean = hasRole(Role.ADMIN)

    fun isManager(): Boolean = hasRole(Role.MANAGER)

    companion object {
        fun ofUser(): Roles = Roles(mutableSetOf(Role.USER))

        fun of(vararg roles: Role): Roles {
            require(roles.isNotEmpty()) { "최소 하나의 역할이 필요합니다" }
            return Roles(roles.toMutableSet())
        }
    }
}
