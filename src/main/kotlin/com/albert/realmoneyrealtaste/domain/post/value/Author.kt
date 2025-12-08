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

    @Column(name = "author_introduction", nullable = false, length = MAX_INTRODUCTION_LENGTH)
    val introduction: String,

    @Column(name = "author_image_id", nullable = false)
    val imageId: Long,
) {

    companion object {
        const val MAX_NICKNAME_LENGTH = 20
        const val MAX_INTRODUCTION_LENGTH = 500

        const val ERROR_NICKNAME_BLANK = "작성자 닉네임은 필수입니다."
        const val ERROR_NICKNAME_LENGTH = "닉네임은 20자 이내여야 합니다."

        const val ERROR_IMAGE_ID_POSITIVE = "작성자 이미지 ID는 0보다 커야 합니다."
    }

    init {
        require(nickname.isNotBlank()) { ERROR_NICKNAME_BLANK }
        require(nickname.length <= MAX_NICKNAME_LENGTH) { ERROR_NICKNAME_LENGTH }
        require(imageId > 0) { ERROR_IMAGE_ID_POSITIVE }
    }
}
