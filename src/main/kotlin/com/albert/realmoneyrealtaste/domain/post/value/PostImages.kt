package com.albert.realmoneyrealtaste.domain.post.value

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidPostImagesException
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
    @Column(name = "image_url", nullable = false, length = 500)
    val urls: List<String>,
) {
    init {
        validateImages(urls)
    }

    private fun validateImages(urls: List<String>) {
        if (urls.size > MAX_IMAGE_COUNT) {
            throw InvalidPostImagesException(
                "이미지는 최대 ${MAX_IMAGE_COUNT}장까지 업로드 가능합니다. 현재: ${urls.size}장"
            )
        }
        urls.forEach { url ->
            if (url.isBlank()) {
                throw InvalidPostImagesException("이미지 URL은 필수입니다.")
            }
            if (url.length > 500) {
                throw InvalidPostImagesException("이미지 URL은 500자를 초과할 수 없습니다.")
            }
        }
    }

    fun isEmpty(): Boolean = urls.isEmpty()

    fun isNotEmpty(): Boolean = urls.isNotEmpty()

    fun size(): Int = urls.size

    companion object {
        const val MAX_IMAGE_COUNT = 5

        fun empty(): PostImages = PostImages(emptyList())

        fun of(vararg urls: String): PostImages = PostImages(urls.toList())
    }
}
