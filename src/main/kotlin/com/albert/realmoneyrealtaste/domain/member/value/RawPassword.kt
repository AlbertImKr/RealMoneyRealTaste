package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.exceptions.PasswordValidationException

class RawPassword(val value: String) {
    init {
        if (value.isBlank()) throw PasswordValidationException.Required()

        if (value.length !in 8..20) throw PasswordValidationException.InvalidLength()

        if (!value.any { it.isDigit() }) throw PasswordValidationException.MissingDigit()

        if (!value.any { it.isLowerCase() }) throw PasswordValidationException.MissingLowerCase()

        if (!value.any { it.isUpperCase() }) throw PasswordValidationException.MissingUpperCase()

        if (!value.any { !it.isLetterOrDigit() }) throw PasswordValidationException.MissingSpecialChar()

        if (value.any { !it.isLetterOrDigit() && !ALLOWED_SPECIAL_CHARS_SET.contains(it) }) {
            throw PasswordValidationException.InvalidSpecialChar(ALLOWED_SPECIAL_CHARS)
        }
    }

    companion object {
        const val ALLOWED_SPECIAL_CHARS = "!@#$%^&*"
        private val ALLOWED_SPECIAL_CHARS_SET: Set<Char> = ALLOWED_SPECIAL_CHARS.toSet()
    }
}
