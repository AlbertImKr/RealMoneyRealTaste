package com.albert.realmoneyrealtaste.domain.member.value

enum class TrustLevel(val scoreRange: IntRange) {
    BRONZE(0..199),
    SILVER(200..499),
    GOLD(500..799),
    DIAMOND(800..1000);

    companion object {
        const val ERROR_INVALID_SCORE = "유효한 신뢰 점수가 아닙니다"

        fun fromScore(score: Int): TrustLevel {
            return entries.firstOrNull { score in it.scoreRange }
                ?: throw IllegalArgumentException(ERROR_INVALID_SCORE)
        }
    }
}
