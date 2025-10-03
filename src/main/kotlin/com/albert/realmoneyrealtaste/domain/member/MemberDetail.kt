package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import java.time.LocalDateTime

@Embeddable
class MemberDetail private constructor(
    profileAddress: ProfileAddress?,
    introduction: Introduction?,
    val registeredAt: LocalDateTime,
    activatedAt: LocalDateTime?,
    deactivatedAt: LocalDateTime?,
) {
    @Embedded
    final var profileAddress: ProfileAddress? = profileAddress
        private set

    @Embedded
    final var introduction: Introduction? = introduction
        private set

    @Column(name = "activated_at")
    final var activatedAt: LocalDateTime? = activatedAt
        private set

    @Column(name = "deactivated_at")
    final var deactivatedAt: LocalDateTime? = deactivatedAt
        private set

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

