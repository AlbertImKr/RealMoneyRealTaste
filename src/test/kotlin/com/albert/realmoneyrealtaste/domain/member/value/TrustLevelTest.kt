package com.albert.realmoneyrealtaste.domain.member.value

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFailsWith

class TrustLevelTest {

    @ParameterizedTest
    @CsvSource(
        value = [
            "0, BRONZE",
            "199, BRONZE",
            "200, SILVER",
            "499, SILVER",
            "500, GOLD",
            "799, GOLD",
            "800, DIAMOND",
            "1000, DIAMOND"
        ]
    )
    fun `fromScore - success - returns correct trust level for given score`(score: Int, expectedLevel: TrustLevel) {
        val actualLevel = TrustLevel.fromScore(score)

        assertEquals(expectedLevel, actualLevel, "Score: $score")
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, 1001, 1500])
    fun `fromScore - failure - throws exception when score is out of range`(invalidScore: Int) {
        assertFailsWith<IllegalArgumentException> {
            TrustLevel.fromScore(invalidScore)
        }.let {
            assertEquals("유효한 신뢰 점수가 아닙니다", it.message)
        }
    }
}
