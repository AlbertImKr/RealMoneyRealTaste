package com.albert.realmoneyrealtaste.domain.member.value

class RawPassword(val value: String) {
    init {
        validate()
    }

    private fun validate() {
        require(value.isNotBlank()) { ERROR_REQUIRED }

        require(value.length in MIN_LENGTH..MAX_LENGTH) { ERROR_INVALID_LENGTH }

        require(value.any { it.isDigit() }) { ERROR_MISSING_DIGIT }

        require(value.any { it.isLowerCase() }) { ERROR_MISSING_LOWERCASE }

        require(value.any { it.isUpperCase() }) { ERROR_MISSING_UPPERCASE }

        require(value.any { !it.isLetterOrDigit() }) { ERROR_MISSING_SPECIAL_CHAR }

        require(value.all { it.isLetterOrDigit() || it in ALLOWED_SPECIAL_CHARS_SET }) {
            ERROR_INVALID_SPECIAL_CHAR
        }
    }

    companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 20

        const val ALLOWED_SPECIAL_CHARS = "!@#$%^&*"

        const val ERROR_REQUIRED = "비밀번호는 필수입니다"
        const val ERROR_INVALID_LENGTH = "비밀번호는 $MIN_LENGTH ~ $MAX_LENGTH 자 사이여야 합니다"
        const val ERROR_MISSING_DIGIT = "비밀번호에는 최소한 하나의 숫자가 포함되어야 합니다"
        const val ERROR_MISSING_LOWERCASE = "비밀번호에는 최소한 하나의 소문자가 포함되어야 합니다"
        const val ERROR_MISSING_UPPERCASE = "비밀번호에는 최소한 하나의 대문자가 포함되어야 합니다"
        const val ERROR_MISSING_SPECIAL_CHAR = "비밀번호에는 최소한 하나의 특수문자가 포함되어야 합니다"
        const val ERROR_INVALID_SPECIAL_CHAR = "비밀번호에 허용되지 않는 특수문자가 포함되어 있습니다. 허용되는 특수문자: $ALLOWED_SPECIAL_CHARS"

        private val ALLOWED_SPECIAL_CHARS_SET: Set<Char> = ALLOWED_SPECIAL_CHARS.toSet()
    }
}
