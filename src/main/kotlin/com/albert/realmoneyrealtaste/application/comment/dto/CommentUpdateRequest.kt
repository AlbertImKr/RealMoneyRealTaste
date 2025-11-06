package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.CommentUpdateRequestException

data class CommentUpdateRequest(
    val commentId: Long,
    val content: String,
    val memberId: Long,
) {
    init {
        validate()
    }

    private fun validate() {
        if (commentId <= 0) {
            throw CommentUpdateRequestException.InvalidCommentIdException("댓글 ID는 양수여야 합니다: $commentId")
        }

        if (memberId <= 0) {
            throw CommentUpdateRequestException.InvalidCommentIdException("회원 ID는 양수여야 합니다: $memberId")
        }

        if (content.isBlank()) {
            throw CommentUpdateRequestException.EmptyContentException("댓글 내용은 필수입니다.")
        }

        if (content.length > 500) {
            throw CommentUpdateRequestException.ExceedContentLengthException("댓글 내용은 500자를 초과할 수 없습니다.")
        }
    }
}
