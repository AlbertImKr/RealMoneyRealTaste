package com.albert.realmoneyrealtaste.domain.member.exceptions

import com.albert.realmoneyrealtaste.domain.common.ValueObjectValidationException

/**
 * 프로필 주소 검증 예외
 */
sealed class ProfileAddressValidationException(message: String) : ValueObjectValidationException(message) {
    class InvalidLength : ProfileAddressValidationException("프로필 주소는 3-15자 사이여야 합니다")
    class InvalidFormat : ProfileAddressValidationException("프로필 주소는 영문, 숫자, 한글만 사용 가능합니다")
}
