package com.albert.realmoneyrealtaste.adapter.infrastructure.security

import com.albert.realmoneyrealtaste.application.member.exception.MemberVerifyException
import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
    private val memberVerify: MemberVerify,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val email = Email(authentication.name)
        val password = RawPassword(authentication.credentials.toString())

        val memberPrincipal = try {
            memberVerify.verify(email, password)
        } catch (e: MemberVerifyException) {
            throw BadCredentialsException("비밀번호 또는 이메일이 일치하지 않습니다.", e)
        }

        val authorities = memberPrincipal.getAuthorities()

        return UsernamePasswordAuthenticationToken(
            memberPrincipal,
            null,
            authorities,
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
