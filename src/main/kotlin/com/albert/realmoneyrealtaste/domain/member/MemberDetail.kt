package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@ConsistentCopyVisibility
@Entity
@Table(name = "member_details")
data class MemberDetail private constructor(
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

    final override fun equals(other: Any?): Boolean = super.equals(other)

    final override fun hashCode(): Int = super.hashCode()

    @Override
    override fun toString(): String = super.toString()

    companion object {
        fun register(profileAddress: ProfileAddress?, introduction: Introduction?): MemberDetail =
            MemberDetail(
                profileAddress = profileAddress,
                introduction = introduction,
                registeredAt = LocalDateTime.now()
            )
    }
}

