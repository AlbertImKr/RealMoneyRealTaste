package com.albert.realmoneyrealtaste.domain.friend.command

/**
 * 친구 요청 명령
 */
data class FriendRequestCommand(
    val fromMemberId: Long,
    val fromMemberNickName: String,
    val toMemberId: Long,
    val toMemberNickname: String,
) {
    companion object {
        const val ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE = "요청자 회원 ID는 양수여야 합니다"
        const val ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE = "대상 회원 ID는 양수여야 합니다"
        const val ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF = "자기 자신에게는 친구 요청을 보낼 수 없습니다"
        const val ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY = "대상 회원 닉네임은 비어있을 수 없습니다"
        const val ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY = "요청자 회원 닉네임은 비어있을 수 없습니다"
    }

    init {
        validate()
    }

    private fun validate() {
        require(fromMemberId > 0) { ERROR_FROM_MEMBER_ID_MUST_BE_POSITIVE }
        require(toMemberId > 0) { ERROR_TO_MEMBER_ID_MUST_BE_POSITIVE }
        require(fromMemberId != toMemberId) { ERROR_CANNOT_REQUEST_FRIENDSHIP_TO_YOURSELF }
        require(fromMemberNickName.isNotBlank()) { ERROR_FROM_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY }
        require(toMemberNickname.isNotBlank()) { ERROR_TO_MEMBER_NICKNAME_MUST_NOT_BE_EMPTY }
    }
}
