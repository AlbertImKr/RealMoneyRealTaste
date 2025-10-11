package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "password_reset_tokens",
    indexes = [
        Index(name = "idx_password_reset_token", columnList = "token"),
        Index(name = "idx_password_reset_member_id", columnList = "member_id")
    ]
)
class PasswordResetToken(
    memberId: Long,

    token: String,

    createdAt: LocalDateTime,

    expiresAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "member_id", nullable = false)
    var memberId: Long = memberId
        protected set

    @Column(name = "token", nullable = false, unique = true)
    var token: String = token
        protected set

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = expiresAt
        protected set

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
