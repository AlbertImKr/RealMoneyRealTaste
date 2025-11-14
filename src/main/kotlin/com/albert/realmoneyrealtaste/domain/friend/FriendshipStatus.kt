package com.albert.realmoneyrealtaste.domain.friend

/**
 * 친구 관계 상태
 */
enum class FriendshipStatus {
    PENDING,      // 친구 요청 대기
    ACCEPTED,     // 친구 관계 성립
    REJECTED,     // 친구 요청 거절
    UNFRIENDED    // 친구 관계 해제
}
