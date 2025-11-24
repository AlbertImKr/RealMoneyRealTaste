package com.albert.realmoneyrealtaste.domain.collection.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CollectionOwner(
    @Column(name = "owner_member_id", nullable = false)
    val memberId: Long,

    @Column(name = "owner_nickname", nullable = false)
    val nickname: String,
) {
    init {
        validate()
    }

    private fun validate() {
        require(memberId > 0) { "소유자 회원 ID는 양수여야 합니다." }
        require(nickname.isNotEmpty()) { "소유자 닉네임은 비어있을 수 없습니다." }
    }
}
