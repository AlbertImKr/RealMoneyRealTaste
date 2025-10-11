package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.exceptions.EmailValidationException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.NaturalId

@Embeddable
data class Email(
    @NaturalId
    @Column(name = "email", nullable = false, unique = true)
    val address: String,
) {
    init {
        validate()
    }

    private fun validate() {
        if (address.isBlank()) {
            throw EmailValidationException.Required()
        }

        val emailRegex = EMAIL_PATTERN.toRegex()
        if (!emailRegex.matches(address)) {
            throw EmailValidationException.InvalidFormat()
        }
    }

    fun getDomain(): String = address.substringAfter("@")

    companion object {
        private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$"
    }
}
