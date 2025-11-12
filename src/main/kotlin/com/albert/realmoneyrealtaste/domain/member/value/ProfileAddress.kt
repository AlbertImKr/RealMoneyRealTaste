package com.albert.realmoneyrealtaste.domain.member.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ProfileAddress(
    @Column(name = COLUMN_NAME, length = MAX_LENGTH, unique = true)
    val address: String,
) {
    init {
        validate()
    }

    private fun validate() {
        if (address.isBlank()) return

        require(address.length in MIN_LENGTH..MAX_LENGTH) { ERROR_INVALID_LENGTH }

        val profileRegex = PATTERN.toRegex()
        require(profileRegex.matches(address)) { ERROR_INVALID_FORMAT }
    }

    companion object {
        const val COLUMN_NAME = "profile_address"

        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 15

        const val ERROR_INVALID_LENGTH = "프로필 주소는 $MIN_LENGTH ~ $MAX_LENGTH 자 사이여야 합니다"
        const val ERROR_INVALID_FORMAT = "프로필 주소는 한글, 영문, 숫자만 사용할 수 있습니다"

        const val PATTERN = "^[a-zA-Z0-9가-힣]+$"
    }
}
