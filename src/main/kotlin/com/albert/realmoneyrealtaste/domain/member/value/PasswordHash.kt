package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class PasswordHash protected constructor(
    @Column(name = COLUMN_NAME, nullable = false)
    private val hash: String,
) {

    init {
        validate()
    }

    private fun validate() {
        require(hash.isNotBlank()) { ERROR_REQUIRED }
    }

    fun matches(rawPassword: RawPassword, encoder: PasswordEncoder): Boolean {
        return encoder.matches(rawPassword, hash)
    }

    override fun toString(): String = TO_STRING_MASK

    companion object {
        const val COLUMN_NAME = "password_hash"

        const val ERROR_REQUIRED = "비밀번호 해시는 필수입니다"

        const val TO_STRING_MASK = "비밀번호 해시(보안상 출력 불가)"

        fun of(password: RawPassword, encoder: PasswordEncoder): PasswordHash {
            return PasswordHash(encoder.encode(password))
        }
    }
}
