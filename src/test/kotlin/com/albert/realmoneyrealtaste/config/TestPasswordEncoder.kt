package com.albert.realmoneyrealtaste.config

import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder

class TestPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: com.albert.realmoneyrealtaste.domain.member.value.RawPassword): String {
        return "hashed-${rawPassword.value}"
    }

    override fun matches(
        rawPassword: com.albert.realmoneyrealtaste.domain.member.value.RawPassword,
        passwordHash: String,
    ): Boolean {
        return passwordHash == encode(rawPassword)
    }
}
