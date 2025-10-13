package com.albert.realmoneyrealtaste.application.post.dto

data class PostSearchCondition(
    val restaurantName: String?,
    val authorNickname: String?,
    val minRating: Int?,
    val maxRating: Int?,
)
