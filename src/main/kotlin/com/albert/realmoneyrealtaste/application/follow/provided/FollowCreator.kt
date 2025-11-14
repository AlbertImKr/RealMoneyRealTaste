package com.albert.realmoneyrealtaste.application.follow.provided

import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.domain.follow.command.FollowCreateCommand

/**
 * 팔로우 생성 포트
 */
fun interface FollowCreator {
    fun follow(command: FollowCreateCommand): Follow
}
