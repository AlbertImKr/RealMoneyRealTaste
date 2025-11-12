package com.albert.realmoneyrealtaste.util

import com.albert.realmoneyrealtaste.adapter.security.MemberPrincipal
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.stereotype.Component

@Component
class WithMockMemberSecurityContextFactory : WithSecurityContextFactory<WithMockMember> {

    @Autowired
    lateinit var testMemberHelper: TestMemberHelper

    override fun createSecurityContext(annotation: WithMockMember): SecurityContext {
        val member = testMemberHelper.createMember(
            email = Email(annotation.email),
            nickname = Nickname(annotation.nickname),
        )
        if (annotation.active) member.activate()

        val principal = MemberPrincipal.from(member)

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
