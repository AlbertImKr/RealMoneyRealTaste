package com.albert.realmoneyrealtaste.application.comment.dto

import com.albert.realmoneyrealtaste.application.comment.exception.ReplyCreateRequestException

/**
 * 댓글 대댓글 생성 요청 DTO
 */
data class ReplyCreateRequest(
    val postId: Long,
    val memberId: Long,
    val content: String,
    val parentCommentId: Long,
) {
    init {
        validate()
    }

    private fun validate() {
        if (postId <= 0) {
            throw ReplyCreateRequestException.InvalidPostIdException("게시글 ID는 양수여야 합니다: $postId")
        }

        if (memberId <= 0) {
            throw ReplyCreateRequestException.InvalidMemberIdException("회원 ID는 양수여야 합니다: $memberId")
        }

        if (parentCommentId <= 0) {
            throw ReplyCreateRequestException.InvalidParentCommentIdException("부모 댓글 ID는 양수여야 합니다: $parentCommentId")
        }

        if (content.isBlank()) {
            throw ReplyCreateRequestException.EmptyContentException("댓글 내용은 필수입니다.")
        }
    }
}
