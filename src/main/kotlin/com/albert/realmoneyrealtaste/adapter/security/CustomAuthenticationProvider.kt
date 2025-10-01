package com.albert.realmoneyrealtaste.adapter.security

import com.albert.realmoneyrealtaste.application.member.provided.MemberVerify
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.RawPassword
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
    private val memberVerify: MemberVerify,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val email = Email(authentication.name)
        val password = RawPassword(authentication.credentials.toString())

        if (!memberVerify.verify(email, password)) {
            throw BadCredentialsException("비밀번호가 올바르지 않습니다.")
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

        return UsernamePasswordAuthenticationToken(
            email,
            password,
            authorities
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
