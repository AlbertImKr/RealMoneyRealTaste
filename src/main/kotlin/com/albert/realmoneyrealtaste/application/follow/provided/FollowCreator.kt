package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.domain.follow.Follow

/**
 * 팔로우 생성 포트
 */
fun interface FollowCreator {

    fun follow(request: FollowCreateRequest): Follow
}
