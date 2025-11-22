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

    @Column(name = "author_nickname", nullable = false, length = MAX_NICKNAME_LENGTH)
    val nickname: String,

    @Column(name = "author_introduction", nullable = false, length = 200)
    val introduction: String,
) {

    companion object {
        const val MAX_NICKNAME_LENGTH = 20

        const val ERROR_NICKNAME_BLANK = "작성자 닉네임은 필수입니다."
        const val ERROR_NICKNAME_LENGTH = "닉네임은 20자 이내여야 합니다."
    }

    init {
        require(nickname.isNotBlank()) { ERROR_NICKNAME_BLANK }
        require(nickname.length <= MAX_NICKNAME_LENGTH) { ERROR_NICKNAME_LENGTH }
    }
}
