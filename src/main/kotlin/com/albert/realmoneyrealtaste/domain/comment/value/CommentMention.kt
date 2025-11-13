package com.albert.realmoneyrealtaste.domain.comment.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CommentMention(
    @Column(name = MEMBER_ID_COLUMN, nullable = false)
    val memberId: Long,

    @Column(name = NICKNAME_COLUMN, nullable = false, length = NICKNAME_MAX_LENGTH)
    val nickname: String,

    @Column(name = START_POSITION_COLUMN, nullable = false)
    val startPosition: Int,

    @Column(name = END_POSITION_COLUMN, nullable = false)
    val endPosition: Int,
) {
    companion object {
        const val MEMBER_ID_COLUMN = "mentioned_member_id"
        const val NICKNAME_COLUMN = "mentioned_nickname"
        const val START_POSITION_COLUMN = "start_position"
        const val END_POSITION_COLUMN = "end_position"

        const val NICKNAME_MAX_LENGTH = 20
        const val START_POSITION_MIN = 0

        const val INVALID_MEMBER_ID_MESSAGE = "유효하지 않은 멤버 ID입니다.: %d"
        const val EMPTY_NICKNAME_MESSAGE = "닉네임은 비어 있을 수 없습니다."
        const val OVERSIZED_NICKNAME_MESSAGE = "닉네임은 $NICKNAME_MAX_LENGTH 자를 초과할 수 없습니다.: %d"
        const val INVALID_START_POSITION_MESSAGE = "시작 위치는 $START_POSITION_MIN 이상이어야 합니다.: %d"
        const val INVALID_END_POSITION_MESSAGE = "끝 위치는 시작 위치보다 커야 합니다."
    }

    init {
        validate()
    }

    /**
     * 멘션 텍스트의 길이
     */
    fun length(): Int = endPosition - startPosition

    /**
     * 특정 위치가 이 멘션 범위에 포함되는지 확인
     */
    fun contains(position: Int): Boolean = position in startPosition until endPosition

    /**
     * 다른 멘션과 위치가 겹치는지 확인
     */
    fun overlaps(other: CommentMention): Boolean {
        return startPosition < other.endPosition && endPosition > other.startPosition
    }

    /**
     * 위치 이동 (텍스트 편집 시 사용)
     */
    fun adjustPosition(offset: Int): CommentMention {
        val newStart = startPosition + offset
        val newEnd = endPosition + offset

        return CommentMention(
            memberId = memberId,
            nickname = nickname,
            startPosition = newStart,
            endPosition = newEnd
        )
    }

    private fun validate() {
        require(memberId > 0) { INVALID_MEMBER_ID_MESSAGE.format(memberId) }

        require(nickname.isNotBlank()) { EMPTY_NICKNAME_MESSAGE }
        require(nickname.length <= NICKNAME_MAX_LENGTH) { OVERSIZED_NICKNAME_MESSAGE.format(nickname.length) }

        require(startPosition >= START_POSITION_MIN) { INVALID_START_POSITION_MESSAGE.format(startPosition) }

        require(endPosition > startPosition) { INVALID_END_POSITION_MESSAGE }
    }
}
