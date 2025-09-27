package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@ConsistentCopyVisibility
@Entity
@Table(name = "member_details")
data class MemberDetail private constructor(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var id: Long? = null,

    @Embedded
    val profileAddress: ProfileAddress? = null,

    @Embedded
    val introduction: Introduction? = null,

    @Column(name = "registered_at", nullable = false)
    val registeredAt: LocalDateTime,

    @Column(name = "activated_at")
    val activatedAt: LocalDateTime? = null,

    @Column(name = "deactivated_at")
    val deactivatedAt: LocalDateTime? = null,
) : BaseEntity() {

    fun activate(): MemberDetail = copy(activatedAt = LocalDateTime.now())

    fun deactivate(): MemberDetail = copy(deactivatedAt = LocalDateTime.now())

    fun updateInfo(profileAddress: ProfileAddress?, introduction: Introduction?): MemberDetail =
        copy(profileAddress = profileAddress, introduction = introduction)

    companion object {
        fun register(profileAddress: ProfileAddress?, introduction: Introduction?): MemberDetail =
            MemberDetail(
                profileAddress = profileAddress,
                introduction = introduction,
                registeredAt = LocalDateTime.now()
            )

        fun register(): MemberDetail = MemberDetail(registeredAt = LocalDateTime.now())
    }
}

