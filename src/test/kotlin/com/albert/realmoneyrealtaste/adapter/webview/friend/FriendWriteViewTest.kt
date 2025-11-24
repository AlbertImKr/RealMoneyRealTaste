package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendWriteViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var friendRequestor: FriendRequestor

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - sends friend request and returns friend button fragment`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val request = SendFriendRequest(
            toMemberId = targetMember.requireId(),
            toMemberNickname = targetMember.nickname.value
        )

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // hasSentFriendRequest가 true로 설정되었는지 확인
        val modelAndView = result.modelAndView!!
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean
        assertEquals(true, hasSentFriendRequest, "친구 요청을 보냈으므로 hasSentFriendRequest는 true여야 함")

        val isFriend = modelAndView.model["isFriend"] as Boolean
        assertEquals(false, isFriend, "아직 친구 관계가 아니므로 isFriend는 false여야 함")
    }

    @Test
    fun `sendFriendRequest - forbidden - when not authenticated`() {
        val request = SendFriendRequest(
            toMemberId = 1L,
            toMemberNickname = "target"
        )

        mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - handles empty friend list initially`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val request = SendFriendRequest(
            toMemberId = targetMember.requireId(),
            toMemberNickname = targetMember.nickname.value
        )

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andReturn()

        // 친구 요청을 보낸 상태 확인
        val modelAndView = result.modelAndView!!
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean
        assertEquals(true, hasSentFriendRequest)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfriend - success - hasSentFriendRequest is false when no request sent`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 친구 요청을 보내지 않고 unfriend를 호출하여 상태 확인
        val result = mockMvc.perform(
            delete(FriendUrls.UNFRIEND, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andReturn()

        // hasSentFriendRequest가 false로 설정되었는지 확인
        val modelAndView = result.modelAndView!!
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean
        assertEquals(false, hasSentFriendRequest, "친구 요청을 보내지 않았으므로 hasSentFriendRequest는 false여야 함")

        val isFriend = modelAndView.model["isFriend"] as Boolean
        assertEquals(false, isFriend, "친구 관계가 아니므로 isFriend는 false여야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - accepts friend request`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // 친구 요청을 수락한 후 상태 확인
        val modelAndView = result.modelAndView!!
        val isFriend = modelAndView.model["isFriend"] as Boolean
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean

        assertEquals(true, isFriend, "친구 요청을 수락했으므로 isFriend는 true여야 함")
        assertEquals(false, hasSentFriendRequest, "친구 관계가 성립되었으므로 hasSentFriendRequest는 false여야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - rejects friend request`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "false")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // 친구 요청을 거절한 후 상태 확인
        val modelAndView = result.modelAndView!!
        val isFriend = modelAndView.model["isFriend"] as Boolean
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean

        assertEquals(false, isFriend, "친구 요청을 거절했으므로 isFriend는 false여야 함")
        assertEquals(false, hasSentFriendRequest, "친구 요청이 거절되었으므로 hasSentFriendRequest는 false여야 함")
    }

    @Test
    fun `respondToFriendRequest - forbidden - when not authenticated`() {
        val receiver = testMemberHelper.createActivatedMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfriend - success - removes friendship`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        val result = mockMvc.perform(
            delete(FriendUrls.UNFRIEND, friendship.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attributeExists("author"))
            .andReturn()

        // 친구 해제 후 상태 확인
        val modelAndView = result.modelAndView!!
        val isFriend = modelAndView.model["isFriend"] as Boolean
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean

        assertEquals(false, isFriend, "친구 관계를 해제했으므로 isFriend는 false여야 함")
        assertEquals(false, hasSentFriendRequest, "친구 관계가 해제되었으므로 hasSentFriendRequest는 false여야 함")
    }

    @Test
    fun `unfriend - forbidden - when not authenticated`() {
        mockMvc.perform(
            delete(FriendUrls.UNFRIEND, 1L)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - validates request data`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val request = SendFriendRequest(
            toMemberId = targetMember.requireId(),
            toMemberNickname = targetMember.nickname.value
        )

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("isFriend"))
        assertTrue(modelAndView.model.containsKey("hasSentFriendRequest"))
        assertTrue(modelAndView.model.containsKey("author"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - updates friend status correctly`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
            .andExpect(model().attributeExists("author"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfriend - success - updates friend status to false`() {
        val result = mockMvc.perform(
            delete(FriendUrls.UNFRIEND, 1L)
        )
            .andExpect(status().isOk)
            .andReturn()

        val modelAndView = result.modelAndView!!
        val isFriend = modelAndView.model["isFriend"] as Boolean
        assertEquals(false, isFriend) // 친구 해제 시 친구 상태가 아님
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - handles invalid member id gracefully`() {
        val request = SendFriendRequest(
            toMemberId = 9999L, // 존재하지 않는 ID
            toMemberNickname = "nonexistent"
        )

        mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError()) // 존재하지 않는 회원이므로 400 에러 발생
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - handles invalid friendship id gracefully`() {
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, 9999L)
                .param("accept", "true")
        )
            .andExpect(status().is4xxClientError())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `unfriend - success - handles invalid friendship id gracefully`() {
        mockMvc.perform(
            delete(FriendUrls.UNFRIEND, 9999L)
        )
            .andExpect(status().isOk) // 존재하지 않는 친구관계도 성공적으로 처리
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - when receiving request hasSentFriendRequest is false`() {
        // 현재 사용자가 친구 요청을 받은 경우 (다른 사용자가 보냄)
        val receiver = testMemberHelper.getDefaultMember() // 현재 사용자
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")

        // sender가 receiver에게 친구 요청 보냄
        val friendship = friendRequestor.sendFriendRequest(
            FriendRequestCommand(
                fromMemberId = sender.requireId(),
                toMemberId = receiver.requireId(),
                toMemberNickname = receiver.nickname.value,
            )
        )

        // receiver(현재 사용자)가 요청에 응답
        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andReturn()

        // 요청을 받은 사용자는 hasSentFriendRequest가 false여야 함
        val modelAndView = result.modelAndView!!
        val hasSentFriendRequest = modelAndView.model["hasSentFriendRequest"] as Boolean
        val isFriend = modelAndView.model["isFriend"] as Boolean

        assertEquals(false, hasSentFriendRequest, "요청을 받은 사용자는 hasSentFriendRequest가 false여야 함")
        assertEquals(true, isFriend, "친구 요청을 수락했으므로 isFriend는 true여야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - hasSentFriendRequest becomes true after sending request`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 처음에는 친구 요청을 보내지 않은 상태 확인
        val initialResult = mockMvc.perform(
            delete(FriendUrls.UNFRIEND, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andReturn()

        val initialModelAndView = initialResult.modelAndView!!
        val initialHasSentFriendRequest = initialModelAndView.model["hasSentFriendRequest"] as Boolean
        assertEquals(false, initialHasSentFriendRequest, "초기 상태에서는 hasSentFriendRequest가 false여야 함")

        // 친구 요청 보내기
        val request = SendFriendRequest(
            toMemberId = targetMember.requireId(),
            toMemberNickname = targetMember.nickname.value
        )

        val afterRequestResult = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        // 친구 요청을 보낸 후 상태 확인
        val afterRequestModelAndView = afterRequestResult.modelAndView!!
        val afterRequestHasSentFriendRequest = afterRequestModelAndView.model["hasSentFriendRequest"] as Boolean
        assertEquals(true, afterRequestHasSentFriendRequest, "친구 요청을 보낸 후에는 hasSentFriendRequest가 true여야 함")
    }
}
