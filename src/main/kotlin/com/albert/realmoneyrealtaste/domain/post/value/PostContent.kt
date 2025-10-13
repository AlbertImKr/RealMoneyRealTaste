package com.albert.realmoneyrealtaste.domain.post.value

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidPostContentException
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
    init {
        validateText(text)
        validateRating(rating)
    }

    private fun validateText(text: String) {
        if (text.isBlank()) {
            throw InvalidPostContentException("게시글 내용은 필수입니다.")
        }
        if (text.length > 2000) {
            throw InvalidPostContentException("게시글 내용은 2000자를 초과할 수 없습니다.")
        }
    }

    private fun validateRating(rating: Int) {
        if (rating !in 1..5) {
            throw InvalidPostContentException("평점은 1에서 5 사이여야 합니다: $rating")
        }
    }

    companion object {
        const val MAX_LENGTH = 2000
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
