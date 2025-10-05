package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToOne
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
class Member protected constructor(
    email: Email,

    nickname: Nickname,

    passwordHash: PasswordHash,

    status: MemberStatus,

    detail: MemberDetail,

    trustScore: TrustScore,

    updatedAt: LocalDateTime,
) : BaseEntity() {

    @Embedded
    var email: Email = email
        protected set

    @Embedded
    var passwordHash: PasswordHash = passwordHash
        protected set

    @Embedded
    var nickname: Nickname = nickname
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus = status
        protected set

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var detail: MemberDetail = detail
        protected set

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var trustScore: TrustScore = trustScore
        protected set

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = updatedAt
        protected set

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
            detail = MemberDetail.register(null, null),
            trustScore = TrustScore.create(),
            status = MemberStatus.PENDING,
            updatedAt = LocalDateTime.now(),
        )
    }
}
