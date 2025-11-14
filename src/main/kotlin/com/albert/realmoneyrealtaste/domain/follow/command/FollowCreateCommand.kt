package com.albert.realmoneyrealtaste.domain.follow.command

data class FollowCreateCommand(
    val followerId: Long,
    val followingId: Long,
) {
    companion object {
        const val ERROR_FOLLOWER_ID_MUST_BE_POSITIVE = "팔로워 ID는 양수여야 합니다"
        const val ERROR_FOLLOWING_ID_MUST_BE_POSITIVE = "팔로잉 대상 ID는 양수여야 합니다"
        const val ERROR_CANNOT_FOLLOW_SELF = "자기 자신을 팔로우할 수 없습니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(followerId > 0) { ERROR_FOLLOWER_ID_MUST_BE_POSITIVE }
        require(followingId > 0) { ERROR_FOLLOWING_ID_MUST_BE_POSITIVE }
        require(followerId != followingId) { ERROR_CANNOT_FOLLOW_SELF }
    }
}
