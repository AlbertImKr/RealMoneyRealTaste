package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidTrustScoreException

enum class TrustLevel(val scoreRange: IntRange) {
    BRONZE(0..199),
    SILVER(200..499),
    GOLD(500..799),
    DIAMOND(800..1000);

    companion object {
        fun fromScore(score: Int): TrustLevel {
            return TrustLevel.entries.firstOrNull { score in it.scoreRange }
                ?: throw InvalidTrustScoreException(score)
        }
    }
}
