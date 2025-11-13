package com.albert.realmoneyrealtaste.domain.post.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 게시글 내용 및 평점
 */
@Embeddable
data class PostContent(
    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    val text: String,

    @Column(name = "rating", nullable = false)
    val rating: Int,
) {
    companion object {
        const val MAX_LENGTH = 2000
        const val MIN_RATING = 1
        const val MAX_RATING = 5

        const val ERROR_TEXT_BLANK = "게시글 내용은 필수입니다."
        const val ERROR_TEXT_LENGTH = "게시글 내용은 $MAX_LENGTH 자를 초과할 수 없습니다."
        const val ERROR_RATING_RANGE = "평점은 $MIN_RATING 에서 $MAX_RATING 사이여야 합니다."
    }

    init {
        validate()
    }

    private fun validate() {
        require(text.isNotBlank()) { ERROR_TEXT_BLANK }
        require(text.length <= MAX_LENGTH) { ERROR_TEXT_LENGTH }
        require(rating in MIN_RATING..MAX_RATING) { ERROR_RATING_RANGE }
    }
}
