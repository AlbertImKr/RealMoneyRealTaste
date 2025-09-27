package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@ConsistentCopyVisibility
@Entity
@Table(
    name = "members",
    indexes = [
        Index(name = "idx_member_email", columnList = "email"),
        Index(name = "idx_member_nickname", columnList = "nickname"),
        Index(name = "idx_member_status", columnList = "status")
    ]
)
data class Member private constructor(
    @Embedded
    val email: Email,

    @Embedded
    val nickname: Nickname,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MemberStatus = MemberStatus.PENDING,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "detail_id")
    val detail: MemberDetail,

    @Embedded
    val trustScore: TrustScore = TrustScore.create(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity() {

    companion object {
        fun register(
            email: Email,
            nickname: Nickname,
            password: String,
            passwordEncoder: PasswordEncoder,
        ): Member = Member(
            email = email,
            nickname = nickname,
            passwordHash = passwordEncoder.encode(password),
            detail = MemberDetail.register(),
        )
    }

    fun activate(): Member {
        require(status == MemberStatus.PENDING) { "등록 대기 상태에서만 등록 완료가 가능합니다" }
        return copy(
            status = MemberStatus.ACTIVE,
            detail = detail.activate()
        )
    }

    fun deactivate(): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 탈퇴가 가능합니다" }
        return copy(
            status = MemberStatus.DEACTIVATED,
            detail = detail.deactivate()
        )
    }

    fun verifyPassword(password: String, passwordEncoder: PasswordEncoder): Boolean =
        passwordEncoder.matches(password, passwordHash)

    fun changePassword(newPassword: String, passwordEncoder: PasswordEncoder): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 비밀번호 변경이 가능합니다" }
        return copy(passwordHash = passwordEncoder.encode(newPassword))
    }

    fun updateInfo(
        nickname: Nickname? = null,
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
    ): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 정보 수정이 가능합니다" }
        return copy(
            nickname = nickname ?: this.nickname,
            detail = detail.updateInfo(profileAddress, introduction)
        )
    }

    fun updateTrustScore(newTrustScore: TrustScore): Member = copy(trustScore = newTrustScore)

    fun canWriteReview(): Boolean = status == MemberStatus.ACTIVE
}
