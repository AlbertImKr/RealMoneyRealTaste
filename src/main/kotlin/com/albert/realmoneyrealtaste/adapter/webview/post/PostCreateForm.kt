package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.application.post.dto.PostCreateRequest
import com.albert.realmoneyrealtaste.domain.post.value.PostContent
import com.albert.realmoneyrealtaste.domain.post.value.PostImages
import com.albert.realmoneyrealtaste.domain.post.value.Restaurant
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class PostCreateForm(
    @field:NotBlank(message = "맛집 이름을 입력해주세요.")
    val restaurantName: String = "",

    @field:NotBlank(message = "맛집 주소를 입력해주세요.")
    val restaurantAddress: String = "",

    @field:Range(min = -90, max = 90, message = "유효한 위도 값을 입력해주세요.")
    val restaurantLatitude: Double = 0.0,

    @field:Range(min = -180, max = 180, message = "유효한 경도 값을 입력해주세요.")
    val restaurantLongitude: Double = 0.0,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val contentText: String = "",

    @field:Range(min = 1, max = 5, message = "평점은 1점 이상 5점 이하로 입력해주세요.")
    val contentRating: Int = 0,

    @field:Size(min = 1, max = 5, message = "이미지는 최대 5장까지 업로드할 수 있습니다.")
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
