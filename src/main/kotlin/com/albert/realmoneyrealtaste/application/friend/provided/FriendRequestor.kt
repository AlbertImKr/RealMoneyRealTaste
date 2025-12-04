package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.domain.friend.Friendship

/**
 * 친구 요청 생성 포트
 */
fun interface FriendRequestor {
    fun sendFriendRequest(fromMemberId: Long, toMemberId: Long): Friendship
}
