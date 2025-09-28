package com.albert.realmoneyrealtaste.domain.member

interface PasswordEncoder {

    fun encode(rawPassword: RawPassword): String

    fun matches(rawPassword: RawPassword, passwordHash: String): Boolean
}
