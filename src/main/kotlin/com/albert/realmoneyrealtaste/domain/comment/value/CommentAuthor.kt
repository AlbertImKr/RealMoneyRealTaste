package com.albert.realmoneyrealtaste.domain.comment.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 댓글 작성자 정보
 */
@Embeddable
data class CommentAuthor(
    @Column(name = "author_member_id", nullable = false)
    val memberId: Long,

    @Column(name = "author_nickname", length = 20, nullable = false)
    val nickname: String,
) {
    init {
        require(memberId > 0) { "회원 ID는 정수여야 합니다." }
        require(nickname.isNotBlank()) { "닉네임은 필수입니다." }
        require(nickname.length <= 20) { "닉네임은 20자를 초과할 수 없습니다." }
    }
}
