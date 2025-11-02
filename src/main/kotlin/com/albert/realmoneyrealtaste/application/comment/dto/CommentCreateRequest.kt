package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreateRequestException

/**
 * 댓글 생성 요청 DTO
 */
data class CommentCreateRequest(
    val postId: Long,
    val memberId: Long,
    val content: String,
) {
    init {
        validate()
    }

    private fun validate() {
        if (postId <= 0) {
            throw CommentCreateRequestException.InvalidPostIdException("게시글 ID는 양수여야 합니다: $postId")
        }

        if (memberId <= 0) {
            throw CommentCreateRequestException.InvalidMemberIdException("회원 ID는 양수여야 합니다: $memberId")
        }

        if (content.isBlank()) {
            throw CommentCreateRequestException.EmptyContentException("댓글 내용은 필수입니다.")
        }
    }
}
