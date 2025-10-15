package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 닉네임 검증 예외
 */
sealed class NicknameValidationException(message: String) : ValueObjectValidationException(message) {
    class Required : NicknameValidationException("닉네임은 필수입니다")
    class InvalidLength : NicknameValidationException("닉네임은 2-20자 사이여야 합니다")
    class InvalidFormat : NicknameValidationException("닉네임은 한글, 영문, 숫자만 사용 가능합니다")
}
