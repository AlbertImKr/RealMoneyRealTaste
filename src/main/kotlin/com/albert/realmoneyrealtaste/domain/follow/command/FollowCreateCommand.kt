package com.albert.realmoneyrealtaste.domain.follow.command

data class FollowCreateCommand(
    val followerId: Long,
    val followerNickname: String,
    val followerProfileImageId: Long,
    val followingId: Long,
    val followingNickname: String,
    val followingProfileImageId: Long,
) {
    companion object {
        const val ERROR_FOLLOWER_ID_MUST_BE_POSITIVE = "팔로워 ID는 양수여야 합니다"
        const val ERROR_FOLLOWING_ID_MUST_BE_POSITIVE = "팔로잉 대상 ID는 양수여야 합니다"
        const val ERROR_CANNOT_FOLLOW_SELF = "자기 자신을 팔로우할 수 없습니다"
        const val ERROR_FOLLOWER_NICKNAME_BLANK = "팔로워 닉네임은 비어있을 수 없습니다"
        const val ERROR_FOLLOWING_NICKNAME_BLANK = "팔로잉 대상 닉네임은 비어있을 수 없습니다"
        const val ERROR_FOLLOWER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE = "팔로워 프로필 이미지 ID는 양수여야 합니다"
        const val ERROR_FOLLOWING_PROFILE_IMAGE_ID_MUST_BE_POSITIVE = "팔로잉 대상 프로필 이미지 ID는 양수여야 합니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(followerId > 0) { ERROR_FOLLOWER_ID_MUST_BE_POSITIVE }
        require(followingId > 0) { ERROR_FOLLOWING_ID_MUST_BE_POSITIVE }
        require(followerId != followingId) { ERROR_CANNOT_FOLLOW_SELF }
        require(followerNickname.isNotBlank()) { ERROR_FOLLOWER_NICKNAME_BLANK }
        require(followingNickname.isNotBlank()) { ERROR_FOLLOWING_NICKNAME_BLANK }
        require(followerProfileImageId > 0) { ERROR_FOLLOWER_PROFILE_IMAGE_ID_MUST_BE_POSITIVE }
        require(followingProfileImageId > 0) { ERROR_FOLLOWING_PROFILE_IMAGE_ID_MUST_BE_POSITIVE }
    }
}
