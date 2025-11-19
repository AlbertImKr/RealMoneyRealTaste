package com.albert.realmoneyrealtaste.adapter.webapi.follow

import jakarta.validation.constraints.NotBlank

class FollowCreateApiRequest(
    @field:NotBlank("팔로잉 닉네임은 비어있을 수 없습니다")
    val followingNickname: String,
)
