package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import java.time.LocalDateTime

@Embeddable
open class MemberDetail protected constructor(
    profileAddress: ProfileAddress?,
    introduction: Introduction?,
    val registeredAt: LocalDateTime,
    activatedAt: LocalDateTime?,
    deactivatedAt: LocalDateTime?,
) {
    @Embedded
    var profileAddress: ProfileAddress? = profileAddress
        protected set

    @Embedded
    var introduction: Introduction? = introduction
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
        this.profileAddress = profileAddress
        this.introduction = introduction
    }

    companion object {
        fun register(profileAddress: ProfileAddress?, introduction: Introduction?): MemberDetail =
            MemberDetail(
                profileAddress = profileAddress,
                introduction = introduction,
                activatedAt = null,
                deactivatedAt = null,
                registeredAt = LocalDateTime.now(),
            )

        fun register(): MemberDetail = MemberDetail(
            profileAddress = null,
            introduction = null,
            activatedAt = null,
            deactivatedAt = null,
            registeredAt = LocalDateTime.now()
        )
    }
}

