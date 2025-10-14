package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 신뢰 점수 검증 예외
 */
class InvalidTrustScoreException(score: Int) : ValueObjectValidationException("유효하지 않은 신뢰 점수입니다: $score")
