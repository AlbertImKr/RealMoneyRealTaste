package com.albert.realmoneyrealtaste.domain.image.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class FileKey(
    @Column(name = "file_key", nullable = false, unique = true)
    val value: String,
) {

    companion object {
        const val MAX_LENGTH = 500

        // 허용되는 문자: 영문, 숫자, 경로(/), 언더스코어(_), 하이픈(-) + 확장자(.영문숫자)
        val FILE_KEY_PATTERN = Regex("^[a-zA-Z0-9._/-]+\\.(jpg|jpeg|png|webp)$")
        const val PATH_TRAVERSAL_SEQUENCE = ".."

        const val ERROR_CANNOT_BE_BLANK = "파일 키는 비어있을 수 없습니다"
        const val ERROR_TOO_LONG = "파일 키가 너무 깁니다 (최대 500자)"
        const val ERROR_INVALID_PATH_TRAVERSAL = "파일 키에 잘못된 경로 문자가 포함되어 있습니다"
        const val ERROR_INVALID_FORMAT = "잘못된 파일 키 형식입니다 (영문, 숫자, _, -, / 만 허용되며 확장자가 필요합니다)"
    }

    init {
        require(value.isNotBlank()) { ERROR_CANNOT_BE_BLANK }
        require(value.length <= MAX_LENGTH) { ERROR_TOO_LONG }
        require(!value.contains(PATH_TRAVERSAL_SEQUENCE)) { ERROR_INVALID_PATH_TRAVERSAL }
        require(value.matches(FILE_KEY_PATTERN)) { ERROR_INVALID_FORMAT }
    }
}
