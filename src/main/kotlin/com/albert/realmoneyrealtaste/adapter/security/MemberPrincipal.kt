package com.albert.realmoneyrealtaste.adapter.security

import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.Serializable

data class MemberPrincipal(
    val memberId: Long,
    val email: Email,
    val nickname: Nickname,
    private val roles: Set<Role>,
) : Serializable {

    fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    }

    companion object {
        fun from(member: Member): MemberPrincipal {
            return MemberPrincipal(
                memberId = member.id!!,
                email = member.email,
                nickname = member.nickname,
                roles = member.roles.getRoles()
            )
        }
    }
}
