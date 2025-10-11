package com.albert.realmoneyrealtaste.domain.member.service

import com.albert.realmoneyrealtaste.domain.member.value.RawPassword

interface PasswordEncoder {

    fun encode(rawPassword: RawPassword): String

    fun matches(rawPassword: RawPassword, passwordHash: String): Boolean
}
