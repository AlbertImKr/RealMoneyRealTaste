package com.albert.realmoneyrealtaste.domain.comment.value

import com.albert.realmoneyrealtaste.domain.comment.exceptions.InvalidCommentContentException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 댓글 내용
 */
@Embeddable
data class CommentContent(
    @Column(name = TEXT_COLUMN, nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
    val text: String,
) {
    init {
        validateText(text)
    }

    private fun validateText(text: String) {
        if (text.isBlank()) {
            throw InvalidCommentContentException(TEXT_REQUIRED_ERROR_MESSAGE)
        }
        if (text.length > MAX_LENGTH) {
            throw InvalidCommentContentException(TEXT_LENGTH_ERROR_MESSAGE)
        }
    }

    /**
     * 멘션 텍스트 추출 (예: @username)
     */
    fun extractMentions(): List<String> {
        val mentionPattern = Regex(MENTION_PATTERN)
        return mentionPattern.findAll(text)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    companion object {
        const val TEXT_COLUMN = "content_text"
        const val TEXT_COLUMN_DEFINITION = "TEXT"

        const val MAX_LENGTH = 500

        const val TEXT_REQUIRED_ERROR_MESSAGE = "댓글 내용은 필수입니다."
        const val TEXT_LENGTH_ERROR_MESSAGE = "댓글 내용은 ${MAX_LENGTH}자를 초과할 수 없습니다."

        const val MENTION_PATTERN = "@([a-zA-Z0-9가-힣]{2,20}) "
    }
}
