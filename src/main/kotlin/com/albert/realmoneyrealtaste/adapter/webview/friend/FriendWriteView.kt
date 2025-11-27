package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.adapter.infrastructure.security.MemberPrincipal
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.dto.UnfriendRequest
import com.albert.realmoneyrealtaste.application.friend.exception.FriendResponseException
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.application.friend.provided.FriendResponder
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipReader
import com.albert.realmoneyrealtaste.application.friend.provided.FriendshipTerminator
import com.albert.realmoneyrealtaste.application.member.provided.MemberReader
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping
class FriendWriteView(
    private val friendRequestor: FriendRequestor,
    private val friendResponder: FriendResponder,
    private val friendshipTerminator: FriendshipTerminator,
    private val friendshipReader: FriendshipReader,
    private val memberReader: MemberReader,
) {

    @PostMapping(FriendUrls.SEND_FRIEND_REQUEST)
    fun sendFriendRequest(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody request: SendFriendRequest,
        model: Model,
    ): String {
        val command = FriendRequestCommand(
            fromMemberId = principal.id,
            toMemberId = request.toMemberId,
            toMemberNickname = request.toMemberNickname,
        )
        val friendship = friendRequestor.sendFriendRequest(command)

        // 상태 업데이트를 위해 모델에 데이터 추가
        model.addAttribute("friendshipId", friendship.id)
        updateFriendButtonModel(principal, request.toMemberId, model)

        return FriendViews.FRIEND_BUTTON
    }

    @PutMapping(FriendUrls.RESPOND_TO_FRIEND_REQUEST)
    fun respondToFriendRequest(
        @PathVariable friendshipId: Long,
        @RequestParam accept: Boolean,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val request = try {
            FriendResponseRequest(
                friendshipId = friendshipId,
                respondentMemberId = principal.id,
                accept = accept
            )
        } catch (e: IllegalArgumentException) {
            throw FriendResponseException("친구 요청을 수락할 수 없습니다.", e)
        }

        val friendship = friendResponder.respondToFriendRequest(request)

        // 상대방 ID를 찾아서 모델 업데이트
        val targetMemberId = friendship.relationShip.memberId

        model.addAttribute("friendshipId", friendshipId)
        updateFriendButtonModel(principal, targetMemberId, model)

        return FriendViews.FRIEND_BUTTON
    }

    @DeleteMapping(FriendUrls.UNFRIEND)
    fun unfriend(
        @PathVariable friendshipId: Long,
        @PathVariable friendMemberId: Long,
        @AuthenticationPrincipal principal: MemberPrincipal,
        model: Model,
    ): String {
        val request = UnfriendRequest(
            memberId = principal.id,
            friendMemberId = friendMemberId
        )

        friendshipTerminator.unfriend(request)

        // 상태 업데이트를 위해 모델에 데이터 추가
        model.addAttribute("friendshipId", friendshipId)
        updateFriendButtonModel(principal, friendMemberId, model)

        return FriendViews.FRIEND_BUTTON
    }

    private fun updateFriendButtonModel(
        principal: MemberPrincipal,
        targetMemberId: Long,
        model: Model,
    ) {
        // 친구 관계 상태 확인
        val isFriend = friendshipReader.isFriend(principal.id, targetMemberId)
        model.addAttribute("isFriend", isFriend)

        // 친구 요청을 보냈는지 확인
        val friendship = friendshipReader.sentedFriendRequest(principal.id, targetMemberId)
        val hasSentFriendRequest = friendship != null
        model.addAttribute("hasSentFriendRequest", hasSentFriendRequest)

        // 템플릿에 필요한 author.id 설정
        model.addAttribute(
            "author",
            memberReader.readMemberById(targetMemberId)
        )
    }
}
