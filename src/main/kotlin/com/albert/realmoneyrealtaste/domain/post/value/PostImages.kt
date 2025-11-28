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
    @Column(name = "image_id", nullable = false)
    val imageIds: List<Long>,
) {
    companion object {
        const val MAX_IMAGE_COUNT = 5

        const val ERROR_MAX_IMAGE_COUNT = "이미지는 최대 $MAX_IMAGE_COUNT 장까지 업로드 가능합니다."
        const val ERROR_IMAGE_ID_NEGATIVE = "이미지 ID는 0보다 커야 합니다."

        fun empty(): PostImages = PostImages(emptyList())

        fun of(vararg imageIds: Long): PostImages = PostImages(imageIds.toList())
    }

    init {
        validate()
    }

    private fun validate() {
        require(imageIds.size <= MAX_IMAGE_COUNT) { ERROR_MAX_IMAGE_COUNT }

        imageIds.forEach { imageId ->
            require(imageId > 0) { ERROR_IMAGE_ID_NEGATIVE }
        }
    }

    fun isEmpty(): Boolean = imageIds.isEmpty()

    fun isNotEmpty(): Boolean = imageIds.isNotEmpty()

    fun size(): Int = imageIds.size

    fun getFirst(): Long? = imageIds.firstOrNull()
}
