package com.albert.realmoneyrealtaste.domain.member.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Nickname(
    @Column(name = COLUMN_NAME, nullable = false, length = MAX_LENGTH)
    val value: String,
) {
    init {
        validate()
    }

    private fun validate() {
        require(value.isNotBlank()) { ERROR_REQUIRED }

        require(value.length in MIN_LENGTH..MAX_LENGTH) { ERROR_LENGTH }

        val nicknameRegex = PATTERN.toRegex()
        require(nicknameRegex.matches(value)) { ERROR_INVALID_FORMAT }
    }

    companion object {
        const val COLUMN_NAME = "nickname"

        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 20

        const val ERROR_REQUIRED = "닉네임은 필수입니다"
        const val ERROR_LENGTH = "닉네임은 $MIN_LENGTH ~ $MAX_LENGTH 자 사이여야 합니다"
        const val ERROR_INVALID_FORMAT = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다"

        const val PATTERN = "^[가-힣a-zA-Z0-9]+$"
    }
}
