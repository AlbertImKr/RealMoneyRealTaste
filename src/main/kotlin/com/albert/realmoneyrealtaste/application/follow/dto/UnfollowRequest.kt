package com.albert.realmoneyrealtaste.application.follow.dto

data class UnfollowRequest(
    val followerId: Long,
    val followingId: Long,
) {
    companion object {
        const val ERROR_MESSAGE_INVALID_FOLLOWER_ID = "팔로워 ID는 양수여야 합니다"
        const val ERROR_MESSAGE_INVALID_FOLLOWING_ID = "팔로잉 대상 ID는 양수여야 합니다"
        const val ERROR_MESSAGE_SELF_UNFOLLOW = "자기 자신을 언팔로우할 수 없습니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(followerId > 0) { ERROR_MESSAGE_INVALID_FOLLOWER_ID }
        require(followingId > 0) { ERROR_MESSAGE_INVALID_FOLLOWING_ID }
        require(followerId != followingId) { ERROR_MESSAGE_SELF_UNFOLLOW }
    }
}
