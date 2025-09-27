package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@ConsistentCopyVisibility
@Embeddable
data class TrustScore private constructor(
    @Column(name = "trust_score")
    val score: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_level")
    val level: TrustLevel = TrustLevel.BRONZE,

    @Column(name = "real_money_review_count")
    val realMoneyReviewCount: Int = 0,

    @Column(name = "ad_review_count")
    val adReviewCount: Int = 0,
) {
    companion object {
        fun create(): TrustScore = TrustScore()

        fun calculateScore(
            realMoneyReviewCount: Int,
            adReviewCount: Int,
            helpfulCount: Int = 0,
            penaltyCount: Int = 0,
        ): Int {
            val baseScore = (realMoneyReviewCount * 5) + (adReviewCount * 1) + (helpfulCount * 2)
            val penalty = penaltyCount * 20
            return maxOf(0, minOf(1000, baseScore - penalty))
        }
    }

    fun addRealMoneyReview(): TrustScore {
        val newCount = realMoneyReviewCount + 1
        val newScore = minOf(1000, score + 5)
        return copy(
            score = newScore,
            level = TrustLevel.fromScore(newScore),
            realMoneyReviewCount = newCount
        )
    }

    fun addAdReview(): TrustScore {
        val newCount = adReviewCount + 1
        val newScore = minOf(1000, score + 1)
        return copy(
            score = newScore,
            level = TrustLevel.fromScore(newScore),
            adReviewCount = newCount
        )
    }

    fun penalize(amount: Int): TrustScore {
        val newScore = maxOf(0, score - amount)
        return copy(
            score = newScore,
            level = TrustLevel.fromScore(newScore)
        )
    }

    fun getRealMoneyRatio(): Double {
        val totalReviews = realMoneyReviewCount + adReviewCount
        return if (totalReviews == 0) 0.0 else realMoneyReviewCount.toDouble() / totalReviews
    }
}
