package com.albert.realmoneyrealtaste.domain.collection.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CollectionInfo(
    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String,

    @Column(name = "cover_image_url", length = 500)
    val coverImageUrl: String?,
) {
    init {
        validateName(name)
        validateDescription(description)
        coverImageUrl?.let { validateCoverImageUrl(it) }
    }

    private fun validateName(name: String) {
        require(name.isNotBlank()) { ERROR_NAME_BLANK }
        require(name.length <= MAX_NAME_LENGTH) { ERROR_NAME_LENGTH_EXCEEDED }
    }

    private fun validateDescription(description: String) {
        require(description.length <= MAX_DESCRIPTION_LENGTH) { ERROR_DESCRIPTION_LENGTH_EXCEEDED }
    }

    private fun validateCoverImageUrl(coverImageUrl: String) {
        require(coverImageUrl.isNotBlank()) { "커버 이미지 URL은 빈 값일 수 없습니다." }
        require(coverImageUrl.length <= MAX_COVER_IMAGE_URL_LENGTH) { ERROR_COVER_IMAGE_URL_LENGTH_EXCEEDED }
    }

    companion object {
        const val MAX_NAME_LENGTH = 100
        const val MAX_DESCRIPTION_LENGTH = 1000
        const val MAX_COVER_IMAGE_URL_LENGTH = 500

        const val ERROR_NAME_BLANK = "컬렉션 이름은 필수입니다."
        const val ERROR_NAME_LENGTH_EXCEEDED = "컬렉션 이름은 ${MAX_NAME_LENGTH}자를 초과할 수 없습니다."
        const val ERROR_DESCRIPTION_LENGTH_EXCEEDED = "컬렉션 설명은 ${MAX_DESCRIPTION_LENGTH}자를 초과할 수 없습니다."
        const val ERROR_COVER_IMAGE_URL_BLANK = "커버 이미지 URL은 빈 값일 수 없습니다."
        const val ERROR_COVER_IMAGE_URL_LENGTH_EXCEEDED = "커버 이미지 URL은 ${MAX_COVER_IMAGE_URL_LENGTH}자를 초과할 수 없습니다."
    }
}
