package com.albert.realmoneyrealtaste.domain.post.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 게시글 작성자 정보
 */
@Embeddable
data class Author(
    @Column(name = "author_member_id", nullable = false)
    val memberId: Long,

    @Column(name = "author_nickname", nullable = false, length = 20)
    val nickname: String,
) {
    init {
        require(nickname.isNotBlank()) { "작성자 닉네임은 필수입니다." }
        require(nickname.length <= 20) { "닉네임은 20자 이내여야 합니다." }
    }
}
