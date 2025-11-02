package com.albert.realmoneyrealtaste.application.comment.dto

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
        require(postId > 0) { "게시글 ID는 양수여야 합니다: $postId" }
        require(memberId > 0) { "회원 ID는 양수여야 합니다: $memberId" }
        require(content.isNotBlank()) { "댓글 내용은 필수입니다." }
        require(parentCommentId > 0) { "부모 댓글 ID는 양수여야 합니다: $parentCommentId" }
    }
}
