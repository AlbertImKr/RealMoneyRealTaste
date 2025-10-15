package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 이메일 검증 예외
 */
sealed class EmailValidationException(message: String) : ValueObjectValidationException(message) {
    class Required : EmailValidationException("이메일은 필수입니다")
    class InvalidFormat : EmailValidationException("유효한 이메일 형식이 아닙니다")
}
