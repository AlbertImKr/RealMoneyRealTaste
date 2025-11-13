package com.albert.realmoneyrealtaste.domain.collection.value

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn

@Embeddable
data class CollectionPosts(
    @ElementCollection
    @CollectionTable(
        name = "collection_posts",
        joinColumns = [JoinColumn(name = "collection_id")]
    )
    @OrderColumn(name = "display_order")
    @Column(name = "post_id", nullable = false)
    val postIds: List<Long>,
) {
    init {
        validate()
    }

    private fun validate() {
        require(postIds.size <= MAX_POST_COUNT) { "컬렉션에는 최대 ${MAX_POST_COUNT}개의 게시글까지 추가할 수 있습니다. 현재: ${postIds.size}개" }

        val duplicateIds = postIds.groupBy { it }.filter { it.value.size > 1 }.keys
        require(duplicateIds.isEmpty()) { "중복된 게시글 ID가 있습니다: $duplicateIds" }

        postIds.forEach { require(it > 0) { "게시글 ID는 양수여야 합니다: $it" } }
    }

    fun add(postId: Long): CollectionPosts {
        require(postId > 0) { "게시글 ID는 양수여야 합니다: $postId" }

        require(!postIds.contains(postId)) { "이미 존재하는 게시글입니다: $postId" }

        return CollectionPosts(postIds + postId)
    }

    fun remove(postId: Long): CollectionPosts {
        require(postIds.contains(postId)) { ("존재하지 않는 게시글입니다: $postId") }

        return CollectionPosts(postIds - postId)
    }

    fun contains(postId: Long): Boolean = postIds.contains(postId)

    fun isEmpty(): Boolean = postIds.isEmpty()

    fun isNotEmpty(): Boolean = postIds.isNotEmpty()

    fun size(): Int = postIds.size

    companion object {
        const val MAX_POST_COUNT = 100

        fun empty(): CollectionPosts = CollectionPosts(emptyList())

        fun of(vararg postIds: Long): CollectionPosts = CollectionPosts(postIds.toList())
    }
}
