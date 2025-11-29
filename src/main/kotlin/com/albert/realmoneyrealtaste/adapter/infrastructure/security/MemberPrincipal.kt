package com.albert.realmoneyrealtaste.adapter.infrastructure.security

import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.Serializable
import java.time.LocalDateTime

data class MemberPrincipal(
    val id: Long,
    val email: Email,
    val nickname: Nickname,
    val active: Boolean,
    val introduction: String,
    val address: String,
    val createdAt: LocalDateTime,
    val profileImageUrl: String = "#",
    private val roles: Set<Role>,
    val followersCount: Long = 0L,
    val followingsCount: Long = 0L,
) : Serializable {

    fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    }

    fun hasRole(role: Role): Boolean {
        return roles.contains(role)
    }

    companion object {
        fun from(member: Member): MemberPrincipal {
            val introValue = member.detail.introduction?.value
            return MemberPrincipal(
                id = member.requireId(),
                email = member.email,
                nickname = member.nickname,
                roles = member.roles.getRoles(),
                active = member.isActive(),
                introduction = introValue ?: "아직 자기소개가 없어요!",
                address = member.detail.address ?: "아직 주소가 없어요!",
                createdAt = member.detail.registeredAt,
            )
        }
    }
}
