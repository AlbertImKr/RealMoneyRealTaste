package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand

/**
 * 친구 요청 생성 포트
 */
fun interface FriendRequestor {
    fun sendFriendRequest(command: FriendRequestCommand): Friendship
}
