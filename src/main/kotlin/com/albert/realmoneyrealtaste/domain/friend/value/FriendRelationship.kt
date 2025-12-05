package com.albert.realmoneyrealtaste.domain.friend.value

import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 친구 관계 정보
 */
@Embeddable
data class FriendRelationship(
    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "member_nickname", length = 50)
    val memberNickname: String,

    @Column(name = "image_id", nullable = false)
    val memberProfileImageId: Long,

    @Column(name = "friend_member_id", nullable = false)
    val friendMemberId: Long,

    @Column(name = "friend_nickname", length = 50)
    val friendNickname: String,

    @Column(name = "friend_image_id", nullable = false)
    val friendProfileImageId: Long,
) {
    companion object {
        const val ERROR_MEMBER_ID_MUST_BE_POSITIVE = "회원 ID는 양수여야 합니다"
        const val ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE = "친구 회원 ID는 양수여야 합니다"
        const val ERROR_CANNOT_FRIEND_YOURSELF = "자기 자신과는 친구가 될 수 없습니다"

        fun of(friendRequestCommand: FriendRequestCommand): FriendRelationship {
            return FriendRelationship(
                memberId = friendRequestCommand.fromMemberId,
                memberNickname = friendRequestCommand.fromMemberNickname,
                memberProfileImageId = friendRequestCommand.fromMemberProfileImageId,
                friendMemberId = friendRequestCommand.toMemberId,
                friendNickname = friendRequestCommand.toMemberNickname,
                friendProfileImageId = friendRequestCommand.toMemberProfileImageId,
            )
        }
    }

    init {
        validate()
    }

    private fun validate() {
        require(memberId > 0) { ERROR_MEMBER_ID_MUST_BE_POSITIVE }
        require(friendMemberId > 0) { ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE }
        require(memberId != friendMemberId) { ERROR_CANNOT_FRIEND_YOURSELF }
    }
}
