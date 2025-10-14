package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidMemberStatusException
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidPasswordException
import com.albert.realmoneyrealtaste.domain.member.exceptions.UnauthorizedRoleOperationException
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.domain.member.value.Role
import com.albert.realmoneyrealtaste.domain.member.value.Roles
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
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

    roles: Roles,

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

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "detail_id", nullable = false, unique = true)
    var detail: MemberDetail = detail
        protected set

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var trustScore: TrustScore = trustScore
        protected set

    @Embedded
    var roles: Roles = roles
        protected set

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = updatedAt
        protected set

    fun activate() {
        if (status != MemberStatus.PENDING) {
            throw InvalidMemberStatusException.NotPending(("등록 대기 상태에서만 등록 완료가 가능합니다"))
        }
        status = MemberStatus.ACTIVE
        detail.activate()
        updatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        if (status != MemberStatus.ACTIVE) {
            throw InvalidMemberStatusException.NotActive(("등록 완료 상태에서만 탈퇴가 가능합니다"))
        }
        status = MemberStatus.DEACTIVATED
        detail.deactivate()
        updatedAt = LocalDateTime.now()
    }

    fun verifyPassword(rawPassword: RawPassword, encoder: PasswordEncoder): Boolean =
        passwordHash.matches(rawPassword, encoder)

    fun changePassword(newPassword: PasswordHash) {
        passwordHash = newPassword
        updatedAt = LocalDateTime.now()
    }

    fun changePassword(currentPassword: RawPassword, newPassword: RawPassword, encoder: PasswordEncoder) {
        if (status != MemberStatus.ACTIVE) {
            throw InvalidMemberStatusException.NotActive(("등록 완료 상태에서만 비밀번호 변경이 가능합니다"))
        }
        if (!passwordHash.matches(currentPassword, encoder)) {
            throw InvalidPasswordException("현재 비밀번호가 일치하지 않습니다")
        }
        passwordHash = PasswordHash.of(newPassword, encoder)
        updatedAt = LocalDateTime.now()
    }

    fun updateInfo(
        nickname: Nickname? = null,
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
    ) {
        if (nickname == null && profileAddress == null && introduction == null) return
        if (status != MemberStatus.ACTIVE) {
            throw InvalidMemberStatusException.NotActive(("등록 완료 상태에서만 정보 수정이 가능합니다"))
        }
        nickname?.let { this.nickname = it }
        detail.updateInfo(profileAddress, introduction)
        updatedAt = LocalDateTime.now()
    }

    fun updateTrustScore(newTrustScore: TrustScore) {
        trustScore = newTrustScore
        updatedAt = LocalDateTime.now()
    }

    fun grantRole(role: Role) {
        if (status != MemberStatus.ACTIVE) {
            throw UnauthorizedRoleOperationException("등록 완료 상태에서만 권한 부여가 가능합니다")
        }
        roles.addRole(role)
        updatedAt = LocalDateTime.now()
    }

    fun revokeRole(role: Role) {
        if (status != MemberStatus.ACTIVE) {
            throw UnauthorizedRoleOperationException("등록 완료 상태에서만 권한 회수가 가능합니다")
        }
        roles.removeRole(role)
        updatedAt = LocalDateTime.now()
    }

    fun isActive(): Boolean = status == MemberStatus.ACTIVE

    fun canManage(): Boolean = status == MemberStatus.ACTIVE && roles.isManager()

    fun canAdministrate(): Boolean = status == MemberStatus.ACTIVE && roles.isAdmin()

    fun hasRole(role: Role): Boolean = roles.hasRole(role)

    fun hasAnyRole(vararg roleList: Role): Boolean = roles.hasAnyRole(*roleList)

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
            roles = Roles.ofUser(),
        )

        fun registerManager(
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
            roles = Roles.of(Role.USER, Role.MANAGER),
        )

        fun registerAdmin(
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
            roles = Roles.of(Role.USER, Role.ADMIN),
        )
    }
}
