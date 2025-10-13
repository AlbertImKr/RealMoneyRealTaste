package com.albert.realmoneyrealtaste.domain.post.value

import com.albert.realmoneyrealtaste.domain.post.exceptions.InvalidRestaurantInfoException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 맛집 정보
 */
@Embeddable
data class Restaurant(
    @Column(name = "restaurant_name", nullable = false, length = 100)
    val name: String,

    @Column(name = "restaurant_address", nullable = false, length = 255)
    val address: String,

    @Column(name = "restaurant_latitude", nullable = false)
    val latitude: Double, // 위도

    @Column(name = "restaurant_longitude", nullable = false)
    val longitude: Double, // 경도
) {
    init {
        validateName(name)
        validateAddress(address)
        validateCoordinates(latitude, longitude)
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw InvalidRestaurantInfoException("맛집 이름은 필수입니다.")
        }
        if (name.length > 100) {
            throw InvalidRestaurantInfoException("맛집 이름은 100자를 초과할 수 없습니다.")
        }
    }

    private fun validateAddress(address: String) {
        if (address.isBlank()) {
            throw InvalidRestaurantInfoException("주소는 필수입니다.")
        }
        if (address.length > 255) {
            throw InvalidRestaurantInfoException("주소는 255자를 초과할 수 없습니다.")
        }
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0) {
            throw InvalidRestaurantInfoException("위도는 -90에서 90 사이여야 합니다: $latitude")
        }
        if (longitude !in -180.0..180.0) {
            throw InvalidRestaurantInfoException("경도는 -180에서 180 사이여야 합니다: $longitude")
        }
    }
}
