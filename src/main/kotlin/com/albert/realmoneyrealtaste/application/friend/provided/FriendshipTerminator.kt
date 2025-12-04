package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest

/**
 * 친구 관계 해제 포트
 */
fun interface FriendshipTerminator {
    fun unfriend(request: UnfriendRequest)
}
