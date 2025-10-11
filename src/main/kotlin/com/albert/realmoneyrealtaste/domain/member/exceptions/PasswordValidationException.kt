package com.albert.realmoneyrealtaste.domain.member.exceptions

/**
 * 비밀번호 검증 예외
 */
sealed class PasswordValidationException(message: String) : ValueObjectValidationException(message) {
    class Required : PasswordValidationException("비밀번호는 필수입니다")
    class InvalidLength : PasswordValidationException("비밀번호는 8자 이상 20자 이하여야 합니다")
    class MissingDigit : PasswordValidationException("비밀번호는 숫자를 포함해야 합니다")
    class MissingLowerCase : PasswordValidationException("비밀번호는 소문자를 포함해야 합니다")
    class MissingUpperCase : PasswordValidationException("비밀번호는 대문자를 포함해야 합니다")
    class MissingSpecialChar : PasswordValidationException("비밀번호는 특수문자를 포함해야 합니다")
    class InvalidSpecialChar(allowedChars: String) :
        PasswordValidationException("비밀번호에 허용되지 않는 특수문자가 포함되어 있습니다. 허용 특수문자: $allowedChars")
}
