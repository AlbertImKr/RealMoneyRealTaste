package com.albert.realmoneyrealtaste.application.friend.provided

import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.domain.friend.Friendship

/**
 * 친구 요청 응답 포트
 */
fun interface FriendResponder {
    fun respondToFriendRequest(request: FriendResponseRequest): Friendship
}
