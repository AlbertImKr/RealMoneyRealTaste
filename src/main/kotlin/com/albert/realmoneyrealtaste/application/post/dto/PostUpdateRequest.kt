package com.albert.realmoneyrealtaste.application.post.dto

import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant

data class PostUpdateRequest(
    val content: PostContent,
    val images: PostImages,
    val restaurant: Restaurant,
)
