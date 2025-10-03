package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "activation_tokens")
class ActivationToken(
    @Column(name = "member_id", nullable = false, unique = true)
    val memberId: Long,
    @Column(name = "token", nullable = false)
    val token: String,
    @Column(name = "created_at")
    val createdAt: LocalDateTime,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity() {

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
