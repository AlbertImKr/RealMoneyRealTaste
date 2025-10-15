package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.application.post.dto.PostUpdateRequest
import com.albert.realmoneyrealtaste.domain.post.Post
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class PostEditForm(
    val id: Long,

    @field:NotBlank(message = "음식점 이름을 입력해주세요.")
    val restaurantName: String,

    @field:NotBlank(message = "음식점 주소를 입력해주세요.")
    val restaurantAddress: String,
    val restaurantLatitude: Double = 0.0,
    val restaurantLongitude: Double = 0.0,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val contentText: String,

    @field:Range(min = 1, max = 5, message = "평점은 1점 이상 5점 이하로 입력해주세요.")
    val contentRating: Int,

    @field:Size(min = 1, max = 5, message = "이미지는 최소 1장, 최대 5장까지 등록할 수 있습니다.")
    val imagesUrls: List<String>,
) {
    fun toPostEditRequest() = PostUpdateRequest(
        restaurant = Restaurant(restaurantName, restaurantAddress, restaurantLatitude, restaurantLongitude),
        content = PostContent(contentText, contentRating),
        images = PostImages(urls = imagesUrls),
    )

    companion object {
        fun fromPost(post: Post) = PostEditForm(
            post.requireId(),
            post.restaurant.name,
            post.restaurant.address,
            post.restaurant.latitude,
            post.restaurant.longitude,
            post.content.text,
            post.content.rating,
            post.images.urls,
        )
    }
}
