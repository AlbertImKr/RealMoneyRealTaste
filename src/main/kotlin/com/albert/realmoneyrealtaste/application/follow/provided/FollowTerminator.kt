package com.albert.realmoneyrealtaste.application.follow.provided

/**
 * 팔로우 해제 포트
 */
fun interface FollowTerminator {
    fun unfollow(
        followerId: Long,
        followingId: Long,
    )
}
