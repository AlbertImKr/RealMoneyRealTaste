package com.albert.realmoneyrealtaste.domain.post.exceptions

/**
 * 음식점 정보가 유효하지 않을 때 발생하는 예외
 */
class InvalidRestaurantInfoException(message: String) : PostDomainException(message)
