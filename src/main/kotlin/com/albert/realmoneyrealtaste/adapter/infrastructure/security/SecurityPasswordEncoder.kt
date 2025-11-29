package com.albert.realmoneyrealtaste.adapter.infrastructure.security

import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class SecurityPasswordEncoder(
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder(),
) : PasswordEncoder {

    override fun encode(rawPassword: RawPassword): String {
        return passwordEncoder.encode(rawPassword.value)
    }

    override fun matches(rawPassword: RawPassword, passwordHash: String): Boolean {
        return passwordEncoder.matches(rawPassword.value, passwordHash)
    }
}
