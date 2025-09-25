package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ProfileAddress(
    @Column(name = "profile_address", length = 15)
    val address: String = "",
) {
    init {
        if (address.isNotBlank()) {
            validate()
        }
    }

    private fun validate() {
        require(address.length in 3..15) { "프로필 주소는 3-15자 사이여야 합니다" }
        val profileRegex = "^[a-zA-Z0-9가-힣]+$".toRegex()
        require(profileRegex.matches(address)) { "프로필 주소는 영문, 숫자, 한글만 사용 가능합니다" }
    }
}
