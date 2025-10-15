package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant

data class PostCreateForm(
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantLatitude: Double = 0.0,
    val restaurantLongitude: Double = 0.0,
    val contentText: String = "",
    val contentRating: Int = 0,
    val imagesUrls: List<String> = emptyList(),
) {
    fun toPostCreateRequest() = PostCreateRequest(
        restaurant = Restaurant(
            name = restaurantName,
            address = restaurantAddress,
            latitude = restaurantLatitude,
            longitude = restaurantLongitude,
        ),
        content = PostContent(
            text = contentText,
            rating = contentRating,
        ),
        images = PostImages(
            urls = imagesUrls,
        ),
    )
}
