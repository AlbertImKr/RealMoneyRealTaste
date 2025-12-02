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
    imageId: Long?,
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

    @Column
    var imageId: Long? = imageId
        protected set

    fun activate() {
        activatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        deactivatedAt = LocalDateTime.now()
    }

    fun updateInfo(
        profileAddress: ProfileAddress? = null,
        introduction: Introduction? = null,
        address: String? = null,
        imageId: Long? = null,
    ) {
        if (profileAddress != null) this.profileAddress = profileAddress
        if (introduction != null) this.introduction = introduction
        if (address != null) this.address = address
        if (imageId != null) this.imageId = imageId
    }

    companion object {
        fun register(
            profileAddress: ProfileAddress? = null,
            introduction: Introduction? = null,
            address: String? = null,
        ): MemberDetail =
            MemberDetail(
                profileAddress = profileAddress,
                introduction = introduction,
                activatedAt = null,
                deactivatedAt = null,
                registeredAt = LocalDateTime.now(),
                address = address,
                imageId = null,
            )
    }
}

