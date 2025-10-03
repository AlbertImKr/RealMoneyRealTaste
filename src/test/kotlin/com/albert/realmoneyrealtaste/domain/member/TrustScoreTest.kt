package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals

class TrustScoreTest {

    @Test
    fun `test create TrustScore`() {
        val trustScore = TrustScore.create()

        assertEquals(0, trustScore.score)
        assertEquals(TrustLevel.BRONZE, trustScore.level)
        assertEquals(0, trustScore.realMoneyReviewCount)
        assertEquals(0, trustScore.adReviewCount)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "1, 0, 5, 1",
            "2, 0, 10, 2",
            "50, 0, 250, 50",
        ]
    )
    fun `test addRealMoneyReview`(times: Int, adTimes: Int, expectedScore: Int) {
        val trustScore = TrustScore.create()
        val expectedLevel = TrustLevel.fromScore(expectedScore)

        repeat(times) {
            trustScore.addRealMoneyReview()
        }

        assertEquals(expectedLevel, trustScore.level)
        assertEquals(adTimes, trustScore.adReviewCount)
        assertEquals(expectedScore, trustScore.score)
        assertEquals(times, trustScore.realMoneyReviewCount)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "1, 0, 1, 1,BRONZE",
            "2, 0, 2, 2,BRONZE",
            "50, 0, 50, 50,BRONZE",
            "200, 0, 200, 200, SILVER",
            "500, 0, 500, 500, GOLD",
            "800, 0, 800, 800, DIAMOND",
        ]
    )
    fun `test addAdReview`(
        times: Int,
        realMoneyTimes: Int,
        expectedScore: Int,
        expectedAdCount: Int,
        expectedLevel: TrustLevel,
    ) {
        val trustScore = TrustScore.create()

        repeat(times) {
            trustScore.addAdReview()
        }

        assertEquals(expectedLevel, trustScore.level)
        assertEquals(realMoneyTimes, trustScore.realMoneyReviewCount)
        assertEquals(expectedScore, trustScore.score)
        assertEquals(expectedAdCount, trustScore.adReviewCount)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "100, 20, 480, SILVER",
            "200, 50, 950, DIAMOND",
            "200, 300, 700, GOLD",
            "50, 100, 150, BRONZE",
        ]
    )
    fun `test penalize`(times: Int, penalty: Int, expectedScore: Int, expectedLevel: TrustLevel) {
        val trustScore = TrustScore.create()
        repeat(times) {
            trustScore.addRealMoneyReview()
        }

        trustScore.penalize(penalty)

        assertEquals(expectedLevel, trustScore.level)
        assertEquals(expectedScore, trustScore.score)
        assertEquals(times, trustScore.realMoneyReviewCount)
        assertEquals(0, trustScore.adReviewCount)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "50, 50, 0.5",
            "30, 70, 0.3",
            "0, 0, 0.0",
            "100, 0, 1.0",
            "0, 100, 0.0",
        ]
    )
    fun `test getRealMoneyRatio`(
        realMoneyCount: Int,
        adCount: Int,
        expectedRatio: Double,
    ) {
        val trustScore = TrustScore.create()
        repeat(realMoneyCount) {
            trustScore.addRealMoneyReview()
        }
        repeat(adCount) {
            trustScore.addAdReview()
        }

        val ratio = trustScore.getRealMoneyRatio()

        assertEquals(expectedRatio, ratio)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "300, 100, 0, 1000",
            "200, 50, 500, 1000",
            "100, 20, 200, 920",
            "0, 0, 0, 0",
            "50, 10, 0, 260",
            "50, 10, 10, 280",
        ]
    )
    fun `test calculateScore`(
        realMoneyCount: Int,
        adCount: Int,
        helpfulCount: Int,
        expectedScore: Int,
    ) {
        val score = TrustScore.calculateScore(
            realMoneyReviewCount = realMoneyCount,
            adReviewCount = adCount,
            helpfulCount = helpfulCount
        )

        assertEquals(expectedScore, score)
    }

    @Test
    fun `test calculateScore - non helpful count`() {
        val score = TrustScore.calculateScore(
            realMoneyReviewCount = 100,
            adReviewCount = 50,
            penaltyCount = 10
        )

        assertEquals(350, score)
    }
}
