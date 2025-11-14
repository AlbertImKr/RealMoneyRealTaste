package com.albert.realmoneyrealtaste.application.friend.dto

/**
 * 친구 해제 명령 (Command Object)
 */
data class UnfriendRequest(
    val memberId: Long,
    val friendMemberId: Long,
) {
    companion object {
        const val ERROR_MEMBER_ID_MUST_BE_POSITIVE = "회원 ID는 양수여야 합니다"
        const val ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE = "친구 회원 ID는 양수여야 합니다"
        const val ERROR_CANNOT_UNFRIEND_SELF = "자기 자신과는 친구 해제할 수 없습니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(memberId > 0) { ERROR_MEMBER_ID_MUST_BE_POSITIVE }
        require(friendMemberId > 0) { ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE }
        require(memberId != friendMemberId) { ERROR_CANNOT_UNFRIEND_SELF }
    }
}
