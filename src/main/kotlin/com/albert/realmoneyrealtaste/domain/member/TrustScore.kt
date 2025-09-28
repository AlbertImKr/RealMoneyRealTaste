package com.albert.realmoneyrealtaste.domain.member

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@ConsistentCopyVisibility
@Embeddable
data class TrustScore private constructor(
    @Column(name = "trust_score")
    val score: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_level")
    val level: TrustLevel,

    @Column(name = "real_money_review_count")
    val realMoneyReviewCount: Int,

    @Column(name = "ad_review_count")
    val adReviewCount: Int,
) {

    internal constructor() : this(0, TrustLevel.BRONZE, 0, 0)

    fun addRealMoneyReview(): TrustScore {
        val newCount = realMoneyReviewCount + 1
        val newScore = minOf(1000, score + REAL_MONEY_REVIEW_WEIGHT)
        return copy(
            score = newScore,
            level = TrustLevel.fromScore(newScore),
            realMoneyReviewCount = newCount
        )
    }

    fun addAdReview(): TrustScore {
        val newCount = adReviewCount + 1
        val newScore = minOf(1000, score + AD_REVIEW_WEIGHT)
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

    companion object {
        const val REAL_MONEY_REVIEW_WEIGHT = 5
        const val AD_REVIEW_WEIGHT = 1
        const val HELPFUL_VOTE_WEIGHT = 2
        const val PENALTY_WEIGHT = 20

        fun create(): TrustScore = TrustScore()

        fun calculateScore(
            realMoneyReviewCount: Int,
            adReviewCount: Int,
            helpfulCount: Int = 0,
            penaltyCount: Int = 0,
        ): Int {
            val realMoneyReviewScore = realMoneyReviewCount * REAL_MONEY_REVIEW_WEIGHT
            val adReviewScore = adReviewCount * AD_REVIEW_WEIGHT
            val helpfulVoteScore = helpfulCount * HELPFUL_VOTE_WEIGHT
            val baseScore = realMoneyReviewScore + adReviewScore + helpfulVoteScore
            val penalty = penaltyCount * PENALTY_WEIGHT
            return maxOf(0, minOf(1000, baseScore - penalty))
        }
    }
}
