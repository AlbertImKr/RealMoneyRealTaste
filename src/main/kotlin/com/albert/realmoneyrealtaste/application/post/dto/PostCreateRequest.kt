package com.albert.realmoneyrealtaste.application.post.dto

import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant

data class PostCreateRequest(
    val restaurant: Restaurant,
    val content: PostContent,
    val images: PostImages,
)
