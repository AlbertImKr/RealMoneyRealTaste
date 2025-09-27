package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Nickname(
    @Column(name = "nickname", nullable = false, unique = true, length = 20)
    val value: String,
) {
    init {
        validate()
    }

    private fun validate() {
        require(value.isNotBlank()) { "닉네임은 필수입니다" }
        require(value.length in 2..20) { "닉네임은 2-20자 사이여야 합니다" }
        val nicknameRegex = "^[가-힣a-zA-Z0-9]+$".toRegex()
        require(nicknameRegex.matches(value)) { "닉네임은 한글, 영문, 숫자만 사용 가능합니다" }
    }
}
