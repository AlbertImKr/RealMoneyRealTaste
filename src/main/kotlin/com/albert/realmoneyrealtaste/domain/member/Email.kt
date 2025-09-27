package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Email(
    @Column(name = "email", nullable = false, unique = true)
    val address: String,
) {
    init {
        validate()
    }

    private fun validate() {
        require(address.isNotBlank()) { EMAIL_REQUIRED_MESSAGE }
        val emailRegex = EMAIL_PATTERN.toRegex()
        require(emailRegex.matches(address)) { EMAIL_INVALID_MESSAGE }
    }

    fun getDomain(): String = address.substringAfter("@")

    companion object {
        private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$"
        private const val EMAIL_REQUIRED_MESSAGE = "이메일은 필수입니다"
        private const val EMAIL_INVALID_MESSAGE = "유효하지 않은 이메일 형식입니다"
    }
}
