package com.albert.realmoneyrealtaste.domain.follow

/**
 * 팔로우 상태
 */
enum class FollowStatus {
    ACTIVE,       // 정상 팔로우 상태
    UNFOLLOWED,   // 언팔로우됨
    BLOCKED,       // 관리자에 의해 차단된 팔로우
}
