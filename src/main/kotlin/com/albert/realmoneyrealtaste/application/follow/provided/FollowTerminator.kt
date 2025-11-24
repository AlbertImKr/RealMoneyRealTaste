package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.application.follow.dto.UnfollowRequest

/**
 * 팔로우 해제 포트
 */
fun interface FollowTerminator {
    fun unfollow(request: UnfollowRequest)
}
