package com.albert.realmoneyrealtaste.domain.member.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Introduction(
    @Column(name = COLUMN_NAME, length = MAX_LENGTH)
    val value: String = "",
) {
    init {
        validate()
    }

    private fun validate() = require(value.length <= MAX_LENGTH) { ERROR_LENGTH }

    companion object {
        const val COLUMN_NAME = "introduction"

        const val MAX_LENGTH = 500

        const val ERROR_LENGTH = "자기소개는 최대 $MAX_LENGTH 자까지 작성할 수 있습니다"
    }
}
