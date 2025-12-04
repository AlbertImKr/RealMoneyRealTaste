package com.albert.realmoneyrealtaste.application.collection.dto

import com.albert.realmoneyrealtaste.domain.collection.PostCollection
import com.albert.realmoneyrealtaste.domain.post.Post
import org.springframework.data.domain.Page

data class PostCollectionDetailResponse(
    val collection: PostCollection,
    val posts: List<Post>,
    val myPosts: Page<Post>,
)
