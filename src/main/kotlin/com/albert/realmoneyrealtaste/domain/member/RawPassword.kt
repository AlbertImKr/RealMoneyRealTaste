package com.albert.realmoneyrealtaste.domain.member

class RawPassword(val value: String) {
    init {
        require(value.isNotBlank()) { "비밀번호는 필수입니다" }
        require(value.length in 8..20) { "비밀번호는 8자 이상 20자 이하여야 합니다" }
        require(value.any { it.isDigit() }) { "비밀번호는 숫자를 포함해야 합니다" }
        require(value.any { it.isLowerCase() }) { "비밀번호는 소문자를 포함해야 합니다" }
        require(value.any { it.isUpperCase() }) { "비밀번호는 대문자를 포함해야 합니다" }
        require(value.any { !it.isLetterOrDigit() }) { "비밀번호는 특수문자를 포함해야 합니다" }
        require(value.all { it.isLetterOrDigit() || ALLOWED_SPECIAL_CHARS_SET.contains(it) }) {
            "비밀번호에 허용되지 않는 특수문자가 포함되어 있습니다. 허용 특수문자: $ALLOWED_SPECIAL_CHARS"
        }
    }

    companion object {
        const val ALLOWED_SPECIAL_CHARS = "!@#$%^&*"
        private val ALLOWED_SPECIAL_CHARS_SET: Set<Char> = ALLOWED_SPECIAL_CHARS.toSet()
    }
}
