package com.albert.realmoneyrealtaste.application.follow.exception

/**
 * 팔로우 애플리케이션 관련 예외의 최상위 클래스
 */
sealed class FollowApplicationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
