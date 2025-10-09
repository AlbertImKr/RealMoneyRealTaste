package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.Role
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockMemberSecurityContextFactory : WithSecurityContextFactory<WithMockMember> {
    override fun createSecurityContext(annotation: WithMockMember): SecurityContext {
        val principal = MemberPrincipal(
            memberId = annotation.memberId,
            email = Email(annotation.email),
            nickname = Nickname(annotation.nickname),
            roles = annotation.roles
                .map { role -> Role.valueOf(role) }
                .toSet()
        )

        val auth = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        return context
    }
}
