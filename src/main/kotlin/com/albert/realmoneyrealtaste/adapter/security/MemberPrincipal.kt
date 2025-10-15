package com.albert.realmoneyrealtaste.adapter.security

import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.Serializable

data class MemberPrincipal(
    val memberId: Long,
    val email: Email,
    val nickname: Nickname,
    val active: Boolean,
    val introduction: String,
    private val roles: Set<Role>,
) : Serializable {

    fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    }

    companion object {
        fun from(member: Member): MemberPrincipal {
            val introValue = member.detail.introduction?.value
            return MemberPrincipal(
                memberId = member.requireId(),
                email = member.email,
                nickname = member.nickname,
                roles = member.roles.getRoles(),
                active = member.isActive(),
                introduction = introValue ?: "아직 자기소개가 없어요!",
            )
        }
    }
}
