package com.albert.realmoneyrealtaste.domain.member

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
        require(value.length <= 500) { "소개는 최대 500자 이내여야 합니다" }
    }
}
