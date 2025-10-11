package com.albert.realmoneyrealtaste.domain.member.value

import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class PasswordHash protected constructor(
    @Column(name = "password_hash", nullable = false)
    private val hash: String,
) {

    init {
        require(hash.isNotBlank()) { "비밀번호 해시는 필수입니다" }
    }

    fun matches(rawPassword: RawPassword, encoder: PasswordEncoder): Boolean {
        return encoder.matches(rawPassword, hash)
    }

    override fun toString(): String {
        return "비밀번호 해시(보안상 출력 불가)"
    }

    companion object {

        fun of(password: RawPassword, encoder: PasswordEncoder): PasswordHash {
            return PasswordHash(encoder.encode(password))
        }
    }
}
