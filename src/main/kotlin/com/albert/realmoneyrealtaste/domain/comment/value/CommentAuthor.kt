package com.albert.realmoneyrealtaste.domain.comment.value

import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentAuthorException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 댓글 작성자 정보
 */
@Embeddable
data class CommentAuthor(
    @Column(name = MEMBER_ID_COLUMN, nullable = false)
    val memberId: Long,

    @Column(name = NICKNAME_COLUMN, length = MAX_NICKNAME_LENGTH, nullable = false)
    val nickname: String,
) {
    init {
        validateMemberId(memberId)
        validateNickname(nickname)
    }

    private fun validateMemberId(memberId: Long) {
        if (memberId < MIN_MEMBER_ID) {
            throw InvalidCommentAuthorException(MEMBER_ID_ERROR_MESSAGE)
        }
    }

    private fun validateNickname(nickname: String) {
        if (nickname.isBlank()) {
            throw InvalidCommentAuthorException(NICKNAME_REQUIRED_ERROR_MESSAGE)
        }
        if (nickname.length > MAX_NICKNAME_LENGTH) {
            throw InvalidCommentAuthorException(NICKNAME_LENGTH_ERROR_MESSAGE)
        }
    }

    companion object {
        const val MEMBER_ID_COLUMN = "author_member_id"
        const val NICKNAME_COLUMN = "author_nickname"

        const val MAX_NICKNAME_LENGTH = 20
        const val MIN_MEMBER_ID = 1L

        const val MEMBER_ID_ERROR_MESSAGE = "회원 ID는 정수여야 합니다."
        const val NICKNAME_REQUIRED_ERROR_MESSAGE = "닉네임은 필수입니다."
        const val NICKNAME_LENGTH_ERROR_MESSAGE = "닉네임은 20자를 초과할 수 없습니다."
    }
}
