package com.albert.realmoneyrealtaste.application.friend.dto

data class FriendResponseRequest(
    val friendshipId: Long,
    val respondentMemberId: Long,
    val accept: Boolean,
) {
    companion object {
        const val ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE = "친구 관계 ID는 양수여야 합니다"
        const val ERROR_RESPONDENT_MEMBER_ID_MUST_BE_POSITIVE = "응답자 회원 ID는 양수여야 합니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(friendshipId > 0) { ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE }
        require(respondentMemberId > 0) { ERROR_RESPONDENT_MEMBER_ID_MUST_BE_POSITIVE }
    }
}
