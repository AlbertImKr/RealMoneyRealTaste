package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "members",
    indexes = [
        Index(name = "idx_member_email", columnList = "email"),
        Index(name = "idx_member_nickname", columnList = "nickname"),
        Index(name = "idx_member_status", columnList = "status")
    ]
)
class Member private constructor(
    @Embedded
    val email: Email,

    nickname: Nickname,

    @Embedded
    private var passwordHash: PasswordHash,

    status: MemberStatus,

    @Embedded
    val detail: MemberDetail,

    trustScore: TrustScore,

    updatedAt: LocalDateTime,
) : BaseEntity() {

    @Embedded
    final var nickname: Nickname = nickname
        private set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    final var status: MemberStatus = status
        private set

    @Embedded
    final var trustScore: TrustScore = trustScore
        private set

    @Column(name = "updated_at")
    final var updatedAt: LocalDateTime = updatedAt
        private set

    fun activate() {
        require(status == MemberStatus.PENDING) { "등록 대기 상태에서만 등록 완료가 가능합니다" }
        status = MemberStatus.ACTIVE
        detail.activate()
        updatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 탈퇴가 가능합니다" }
        status = MemberStatus.DEACTIVATED
        detail.deactivate()
        updatedAt = LocalDateTime.now()
    }

    fun verifyPassword(rawPassword: RawPassword, encoder: PasswordEncoder): Boolean =
        passwordHash.matches(rawPassword, encoder)

    fun changePassword(newPassword: PasswordHash) {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 비밀번호 변경이 가능합니다" }
        passwordHash = newPassword
        updatedAt = LocalDateTime.now()
    }

    fun updateInfo(
        nickname: Nickname? = null,
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
    ) {
        require(status == MemberStatus.ACTIVE) { "등록 완료 상태에서만 정보 수정이 가능합니다" }
        nickname?.let { this.nickname = it }
        detail.updateInfo(profileAddress, introduction)
        updatedAt = LocalDateTime.now()
    }

    fun updateTrustScore(newTrustScore: TrustScore) {
        trustScore = newTrustScore
        updatedAt = LocalDateTime.now()
    }

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
            trustScore = TrustScore.create(),
            status = MemberStatus.PENDING,
            updatedAt = LocalDateTime.now(),
        )
    }
}
