package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.exceptions.IntroductionValidationException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Introduction(
    @Column(name = "introduction", length = 500)
    val value: String = "",
) {
    init {
        validate()
    }

    private fun validate() {
        if (value.length > 500) {
            throw IntroductionValidationException.TooLong()
        }
    }
}
