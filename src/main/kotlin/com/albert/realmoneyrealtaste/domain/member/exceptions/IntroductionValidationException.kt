package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 소개 검증 예외
 */
sealed class IntroductionValidationException(message: String) : ValueObjectValidationException(message) {
    class TooLong : IntroductionValidationException("소개는 최대 500자 이내여야 합니다")
}
