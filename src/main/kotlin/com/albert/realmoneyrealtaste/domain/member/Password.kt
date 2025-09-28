package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Password(
    @Column(name = "password_hash", nullable = false)
    val hash: String,
) {

    init {
        require(hash.isNotBlank()) { "비밀번호 해시는 필수입니다" }
    }
}
