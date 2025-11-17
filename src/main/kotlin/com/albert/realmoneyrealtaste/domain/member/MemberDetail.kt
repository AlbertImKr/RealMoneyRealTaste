package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
class MemberDetail protected constructor(
    profileAddress: ProfileAddress?,
    address: String?,
    introduction: Introduction?,
    val registeredAt: LocalDateTime,
    activatedAt: LocalDateTime?,
    deactivatedAt: LocalDateTime?,
) : BaseEntity() {
    @Embedded
    var profileAddress: ProfileAddress? = profileAddress
        protected set

    @Embedded
    var introduction: Introduction? = introduction
        protected set

    @Column
    var address: String? = address
        protected set

    @Column(name = "activated_at")
    var activatedAt: LocalDateTime? = activatedAt
        protected set

    @Column(name = "deactivated_at")
    var deactivatedAt: LocalDateTime? = deactivatedAt
        protected set

    fun activate() {
        activatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        deactivatedAt = LocalDateTime.now()
    }

    fun updateInfo(profileAddress: ProfileAddress?, introduction: Introduction?) {
        if (profileAddress != null) this.profileAddress = profileAddress
        if (introduction != null) this.introduction = introduction
    }

    companion object {
        fun register(profileAddress: ProfileAddress?, introduction: Introduction?): MemberDetail =
            MemberDetail(
                profileAddress = profileAddress,
                introduction = introduction,
                activatedAt = null,
                deactivatedAt = null,
                registeredAt = LocalDateTime.now(),
                address = null
            )

        fun register(): MemberDetail = MemberDetail(
            profileAddress = null,
            introduction = null,
            activatedAt = null,
            deactivatedAt = null,
            registeredAt = LocalDateTime.now(),
            address = null
        )
    }
}

