package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "activation_tokens")
class ActivationToken(
    memberId: Long,

    token: String,

    createdAt: LocalDateTime,

    expiresAt: LocalDateTime,
) : BaseEntity() {
    @Column(name = "member_id", nullable = false, unique = true)
    var memberId: Long = memberId
        protected set

    @Column(name = "token", nullable = false)
    var token: String = token
        protected set

    @Column(name = "created_at")
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = expiresAt
        protected set

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
