package com.albert.realmoneyrealtaste.domain.member.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.NaturalId
import java.io.Serializable

@Embeddable
data class Email(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @NaturalId
    @Column(name = COLUMN_NAME, nullable = false, unique = true)
    val address: String,
) : Serializable {
    init {
        validate()
    }

    private fun validate() {
        require(address.isNotBlank()) { ERROR_EMPTY }

        val emailRegex = PATTERN.toRegex()
        require(emailRegex.matches(address)) { ERROR_INVALID }
    }

    fun getDomain(): String = address.substringAfter(DELIMITER)

    companion object {
        const val COLUMN_NAME = "email"

        const val PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)*\\.[A-Za-z]{2,}$"

        const val ERROR_EMPTY = "이메일은 필수입니다"
        const val ERROR_INVALID = "유효한 이메일 형식이 아닙니다"

        const val DELIMITER = "@"
    }
}
