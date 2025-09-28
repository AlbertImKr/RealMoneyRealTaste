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
import org.hibernate.annotations.NaturalId
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
    @NaturalId
    @Embedded
    val email: Email,

    @Embedded
    val nickname: Nickname,

    @Embedded
    val passwordHash: PasswordHash,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MemberStatus = MemberStatus.PENDING,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "detail_id")
    val detail: MemberDetail,

    @Embedded
    val trustScore: TrustScore = TrustScore.create(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity() {

    fun activate(): Member {
        require(status == MemberStatus.PENDING) { "등록 대기 상태에서만 등록 완료가 가능합니다" }
        return copy(
            status = MemberStatus.ACTIVE,
            detail = detail.activate(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivate(): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 탈퇴가 가능합니다" }
        return copy(
            status = MemberStatus.DEACTIVATED,
            detail = detail.deactivate(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun verifyPassword(matchPassword: PasswordHash): Boolean = passwordHash == matchPassword

    fun changePassword(newPassword: PasswordHash): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 비밀번호 변경이 가능합니다" }
        return copy(passwordHash = newPassword, updatedAt = LocalDateTime.now())
    }

    fun updateInfo(
        nickname: Nickname? = null,
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
    ): Member {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 정보 수정이 가능합니다" }
        return copy(
            nickname = nickname ?: this.nickname,
            detail = detail.updateInfo(profileAddress, introduction),
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateTrustScore(newTrustScore: TrustScore): Member =
        copy(trustScore = newTrustScore, updatedAt = LocalDateTime.now())

    fun canWriteReview(): Boolean = status == MemberStatus.ACTIVE

    companion object {
        fun register(
            email: Email,
            nickname: Nickname,
            password: PasswordHash,
        ): Member = Member(
            email = email,
            nickname = nickname,
            passwordHash = password,
            detail = MemberDetail.register(),
        )
    }
}
