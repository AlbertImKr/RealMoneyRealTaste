package com.albert.realmoneyrealtaste.application.friend.exception

/**
 * 친구 애플리케이션 관련 예외의 최상위 클래스
 */
sealed class FriendApplicationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)
