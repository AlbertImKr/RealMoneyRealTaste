package com.albert.realmoneyrealtaste.domain.post.value

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn

/**
 * 게시글 이미지 목록 (최대 5장)
 */
@Embeddable
data class PostImages(
    @ElementCollection
    @CollectionTable(
        name = "post_images",
        joinColumns = [JoinColumn(name = "post_id")]
    )
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", nullable = false, length = MAX_URL_LENGTH)
    val urls: List<String>,
) {
    companion object {
        const val MAX_IMAGE_COUNT = 5
        const val MAX_URL_LENGTH = 500

        const val ERROR_MAX_IMAGE_COUNT = "이미지는 최대 $MAX_IMAGE_COUNT 장까지 업로드 가능합니다."
        const val ERROR_URL_BLANK = "이미지 URL은 필수입니다."
        const val ERROR_URL_LENGTH = "이미지 URL은 $MAX_URL_LENGTH 자를 초과할 수 없습니다."

        fun empty(): PostImages = PostImages(emptyList())

        fun of(vararg urls: String): PostImages = PostImages(urls.toList())
    }

    init {
        validate()
    }

    private fun validate() {
        require(urls.size <= MAX_IMAGE_COUNT) { ERROR_MAX_IMAGE_COUNT }

        urls.forEach { url ->
            require(url.isNotBlank()) { ERROR_URL_BLANK }
            require(url.length <= MAX_URL_LENGTH) { ERROR_URL_LENGTH }
        }
    }

    fun isEmpty(): Boolean = urls.isEmpty()

    fun isNotEmpty(): Boolean = urls.isNotEmpty()

    fun size(): Int = urls.size
}
