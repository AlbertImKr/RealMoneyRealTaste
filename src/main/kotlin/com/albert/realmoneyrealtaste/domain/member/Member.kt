package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.common.AggregateRoot
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.common.DomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberActivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberDeactivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberRegisteredDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.PasswordChangedDomainEvent
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

    followersCount: Long,

    followingsCount: Long,

    postCount: Long,
) : BaseEntity(), AggregateRoot {

    @Transient
    private var domainEvents: MutableList<MemberDomainEvent> = mutableListOf()

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

    @Column(name = "follower_count", nullable = false)
    var followersCount: Long = followersCount
        protected set

    @Column(name = "following_count", nullable = false)
    var followingsCount: Long = followingsCount
        protected set

    @Column(name = "post_count", nullable = false)
    var postCount: Long = postCount
        protected set

    val profileImageId: Long
        get() = detail.imageId ?: 1L

    val address: String
        get() = detail.address ?: "푸디마을에 살고 있어요"

    val introduction: String
        get() = detail.introduction?.value ?: "아직 자기소개가 없어요!"

    val registeredAt: LocalDateTime
        get() = detail.registeredAt

    fun activate() {
        require(status == MemberStatus.PENDING) { ERROR_INVALID_STATUS_FOR_ACTIVATION }

        status = MemberStatus.ACTIVE
        detail.activate()
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            MemberActivatedDomainEvent(
                memberId = requireId(),
                email = email.address,
                nickname = nickname.value
            )
        )
    }

    fun deactivate() {
        require(status == MemberStatus.ACTIVE) { ERROR_INVALID_STATUS_FOR_DEACTIVATION }

        status = MemberStatus.DEACTIVATED
        detail.deactivate()
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            MemberDeactivatedDomainEvent(
                memberId = requireId(),
                occurredAt = LocalDateTime.now(),
            )
        )
    }

    fun verifyPassword(rawPassword: RawPassword, encoder: PasswordEncoder): Boolean =
        passwordHash.matches(rawPassword, encoder)

    fun changePassword(newPassword: PasswordHash) {
        passwordHash = newPassword
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            PasswordChangedDomainEvent(
                memberId = requireId(),
                email = email.address
            )
        )
    }

    fun changePassword(currentPassword: RawPassword, newPassword: RawPassword, encoder: PasswordEncoder) {
        require(status == MemberStatus.ACTIVE) { ERROR_INVALID_STATUS_FOR_PASSWORD_CHANGE }

        require(passwordHash.matches(currentPassword, encoder)) { ERROR_INVALID_PASSWORD }

        passwordHash = PasswordHash.of(newPassword, encoder)
        updatedAt = LocalDateTime.now()

        // 도메인 이벤트 발행
        addDomainEvent(
            PasswordChangedDomainEvent(
                memberId = requireId(),
                email = email.address
            )
        )
    }

    fun updateInfo(
        nickname: Nickname? = null,
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
        address: String? = null,
        imageId: Long? = null,
    ) {
        require(status == MemberStatus.ACTIVE) { ERROR_INVALID_STATUS_FOR_INFO_UPDATE }

        val updatedFields = mutableListOf<String>()

        nickname?.let {
            if (nickname != this.nickname) {
                this.nickname = it
                updatedFields.add("nickname")
            }
        }

        if (detail.updateInfo(profileAddress, introduction, address, imageId)) {
            profileAddress?.let { updatedFields.add("profileAddress") }
            introduction?.let { updatedFields.add("introduction") }
            address?.let { updatedFields.add("address") }
            imageId?.let { updatedFields.add("imageId") }
        }

        // 도메인 이벤트 발행
        if (updatedFields.isNotEmpty()) {
            updatedAt = LocalDateTime.now()

            addDomainEvent(
                MemberProfileUpdatedDomainEvent(
                    memberId = requireId(),
                    email = email.address,
                    updatedFields = updatedFields,
                    nickname = if (updatedFields.contains("nickname")) this.nickname.value else null,
                    imageId = if (updatedFields.contains("imageId")) detail.imageId else null
                )
            )
        }
    }

    fun updateTrustScore(newTrustScore: TrustScore) {
        trustScore = newTrustScore
        updatedAt = LocalDateTime.now()
    }

    fun grantRole(role: Role) {
        require(status == MemberStatus.ACTIVE) { ERROR_INVALID_STATUS_FOR_ROLE_CHANGE }

        roles.addRole(role)
        updatedAt = LocalDateTime.now()
    }

    fun revokeRole(role: Role) {
        require(status == MemberStatus.ACTIVE) { ERROR_INVALID_STATUS_FOR_ROLE_CHANGE }

        roles.removeRole(role)
        updatedAt = LocalDateTime.now()
    }

    fun isActive(): Boolean = status == MemberStatus.ACTIVE

    fun canManage(): Boolean = status == MemberStatus.ACTIVE && roles.isManager()

    fun canAdministrate(): Boolean = status == MemberStatus.ACTIVE && roles.isAdmin()

    fun hasRole(role: Role): Boolean = roles.hasRole(role)

    fun hasAnyRole(vararg roleList: Role): Boolean = roles.hasAnyRole(*roleList)

    fun updateFollowersCount(count: Long) {
        followersCount = count
        updatedAt = LocalDateTime.now()
    }

    fun updateFollowingsCount(count: Long) {
        followingsCount = count
        updatedAt = LocalDateTime.now()
    }

    fun updatePostCount(count: Long) {
        postCount = count
        updatedAt = LocalDateTime.now()
    }

    /**
     * 도메인 이벤트 추가
     */
    private fun addDomainEvent(event: MemberDomainEvent) {
        domainEvents.add(event)
    }

    /**
     * 도메인 이벤트를 조회 및 초기화하고 ID를 설정합니다.
     */
    override fun drainDomainEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events.map { it.withMemberId(requireId()) }
    }

    companion object {
        const val ERROR_INVALID_STATUS_FOR_ACTIVATION = "등록 대기 상태에서만 등록 완료가 가능합니다"
        const val ERROR_INVALID_STATUS_FOR_DEACTIVATION = "등록 완료 상태에서만 탈퇴가 가능합니다"
        const val ERROR_INVALID_STATUS_FOR_INFO_UPDATE = "등록 완료 상태에서만 정보 수정이 가능합니다"
        const val ERROR_INVALID_STATUS_FOR_PASSWORD_CHANGE = "등록 완료 상태에서만 비밀번호 변경이 가능합니다"
        const val ERROR_INVALID_PASSWORD = "현재 비밀번호가 일치하지 않습니다"
        const val ERROR_INVALID_STATUS_FOR_ROLE_CHANGE = "등록 완료 상태에서만 권한 변경이 가능합니다"

        fun register(
            email: Email,
            nickname: Nickname,
            password: PasswordHash,
        ): Member {
            val member = Member(
                email = email,
                nickname = nickname,
                passwordHash = password,
                detail = MemberDetail.register(),
                trustScore = TrustScore.create(),
                status = MemberStatus.PENDING,
                updatedAt = LocalDateTime.now(),
                roles = Roles.ofUser(),
                followersCount = 0L,
                followingsCount = 0L,
                postCount = 0L,
            )

            // 도메인 이벤트 발행 (ID는 나중에 설정)
            member.addDomainEvent(
                MemberRegisteredDomainEvent(
                    memberId = 0L, // 임시값, 이벤트 발행 시점에 실제 ID로 설정
                    email = email.address,
                    nickname = nickname.value
                )
            )

            return member
        }

        fun registerManager(
            email: Email,
            nickname: Nickname,
            password: PasswordHash,
        ): Member {
            val member = Member(
                email = email,
                nickname = nickname,
                passwordHash = password,
                detail = MemberDetail.register(),
                trustScore = TrustScore.create(),
                status = MemberStatus.PENDING,
                updatedAt = LocalDateTime.now(),
                roles = Roles.of(Role.USER, Role.MANAGER),
                followersCount = 0L,
                followingsCount = 0L,
                postCount = 0L,
            )

            // 도메인 이벤트 발행 (ID는 나중에 설정)
            member.addDomainEvent(
                MemberRegisteredDomainEvent(
                    memberId = 0L, // 임시값, 이벤트 발행 시점에 실제 ID로 설정
                    email = email.address,
                    nickname = nickname.value
                )
            )

            return member
        }

        fun registerAdmin(
            email: Email,
            nickname: Nickname,
            password: PasswordHash,
        ): Member {
            val member = Member(
                email = email,
                nickname = nickname,
                passwordHash = password,
                detail = MemberDetail.register(),
                trustScore = TrustScore.create(),
                status = MemberStatus.PENDING,
                updatedAt = LocalDateTime.now(),
                roles = Roles.of(Role.USER, Role.ADMIN),
                followersCount = 0L,
                followingsCount = 0L,
                postCount = 0L,
            )

            // 도메인 이벤트 발행 (ID는 나중에 설정)
            member.addDomainEvent(
                MemberRegisteredDomainEvent(
                    memberId = 0L, // 임시값, 이벤트 발행 시점에 실제 ID로 설정
                    email = email.address,
                    nickname = nickname.value
                )
            )

            return member
        }
    }
}
