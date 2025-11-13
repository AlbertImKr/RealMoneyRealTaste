package com.albert.realmoneyrealtaste.domain.post.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 맛집 정보
 */
@Embeddable
data class Restaurant(
    @Column(name = "restaurant_name", nullable = false, length = MAX_NAME_LENGTH)
    val name: String,

    @Column(name = "restaurant_address", nullable = false, length = MAX_ADDRESS_LENGTH)
    val address: String,

    @Column(name = "restaurant_latitude", nullable = false)
    val latitude: Double, // 위도

    @Column(name = "restaurant_longitude", nullable = false)
    val longitude: Double, // 경도
) {
    companion object {
        const val MAX_NAME_LENGTH = 100
        const val MAX_ADDRESS_LENGTH = 255
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0

        const val ERROR_NAME_BLANK = "맛집 이름은 필수입니다."
        const val ERROR_NAME_LENGTH = "맛집 이름은 $MAX_NAME_LENGTH 자를 초과할 수 없습니다."
        const val ERROR_ADDRESS_BLANK = "주소는 필수입니다."
        const val ERROR_ADDRESS_LENGTH = "주소는 $MAX_ADDRESS_LENGTH 자를 초과할 수 없습니다."
        const val ERROR_LATITUDE_RANGE = "위도는 $MIN_LATITUDE 에서 $MAX_LATITUDE 사이여야 합니다."
        const val ERROR_LONGITUDE_RANGE = "경도는 $MIN_LONGITUDE 에서 $MAX_LONGITUDE 사이여야 합니다."
    }

    init {
        validate()
    }

    private fun validate() {
        require(name.isNotBlank()) { ERROR_NAME_BLANK }
        require(name.length <= MAX_NAME_LENGTH) { ERROR_NAME_LENGTH }

        require(address.isNotBlank()) { ERROR_ADDRESS_BLANK }
        require(address.length <= MAX_ADDRESS_LENGTH) { ERROR_ADDRESS_LENGTH }

        require(latitude in MIN_LATITUDE..MAX_LATITUDE) { ERROR_LATITUDE_RANGE }
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) { ERROR_LONGITUDE_RANGE }
    }
}
