package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.exceptions.ProfileAddressValidationException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ProfileAddress(
    @Column(name = "profile_address", length = 15, unique = true)
    val address: String,
) {
    init {
        if (address.isNotBlank()) {
            validate()
        }
    }

    private fun validate() {
        if (address.length !in 3..15) {
            throw ProfileAddressValidationException.InvalidLength()
        }
        val profileRegex = "^[a-zA-Z0-9가-힣]+$".toRegex()
        if (!profileRegex.matches(address)) {
            throw ProfileAddressValidationException.InvalidFormat()
        }
    }
}
