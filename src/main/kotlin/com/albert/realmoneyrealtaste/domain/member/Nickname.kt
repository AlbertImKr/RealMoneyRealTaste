package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.exceptions.NicknameValidationException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Nickname(
    @Column(name = "nickname", nullable = false, length = 20)
    val value: String,
) {
    init {
        validate()
    }

    private fun validate() {
        if (value.isBlank()) {
            throw NicknameValidationException.Required()
        }

        if (value.length !in 2..20) {
            throw NicknameValidationException.InvalidLength()
        }

        val nicknameRegex = "^[가-힣a-zA-Z0-9]+$".toRegex()
        if (!nicknameRegex.matches(value)) {
            throw NicknameValidationException.InvalidFormat()
        }
    }
}
