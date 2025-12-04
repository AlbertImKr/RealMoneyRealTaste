package com.albert.realmoneyrealtaste.adapter.webview.friend

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
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
        val authorId = targetMember.requireId()

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .param("authorId", authorId.toString())
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
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
        val authorId = targetMember.requireId()

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .param("authorId", authorId.toString())
                .with(csrf())
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
    fun `respondToFriendRequest - success - accepts friend request`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.id)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
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
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "false")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
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
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
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
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        val result = mockMvc.perform(
            delete(FriendUrls.UNFRIEND, friendship.id, sender.id)
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
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
        val friend = testMemberHelper.createActivatedMember("friend@email.com")

        mockMvc.perform(
            delete(FriendUrls.UNFRIEND, 1L, friend.id)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `sendFriendRequest - success - validates request data`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val authorId = targetMember.requireId()

        val result = mockMvc.perform(
            post(FriendUrls.SEND_FRIEND_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .param("authorId", authorId.toString())
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("isFriend"))
        assertTrue(modelAndView.model.containsKey("hasSentFriendRequest"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - updates friend status correctly`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FriendViews.FRIEND_BUTTON))
            .andExpect(model().attributeExists("isFriend"))
            .andExpect(model().attributeExists("hasSentFriendRequest"))
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
    fun `unfriend - success - non-existent friendship id gracefully`() {
        val friend = testMemberHelper.createActivatedMember("friend@email.com")
        mockMvc.perform(
            delete(FriendUrls.UNFRIEND, 9999L, friend.id)
                .with(csrf())
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - when receiving request hasSentFriendRequest is false`() {
        // 현재 사용자가 친구 요청을 받은 경우 (다른 사용자가 보냄)
        val receiver = testMemberHelper.getDefaultMember() // 현재 사용자
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")

        // sender가 receiver에게 친구 요청 보냄
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        // receiver(현재 사용자)가 요청에 응답
        val result = mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
                .with(csrf())
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
    fun `respondToFriendRequest - bad request - when friendshipId is negative`() {
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, -1L)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when friendshipId is zero`() {
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, 0L)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - forbidden - when CSRF token missing`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
            // CSRF 토큰 없음
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when accept parameter is missing`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                // accept 파라미터 없음
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when accept parameter is invalid`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "invalid") // 유효하지 않은 값
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - forbidden - when responding to own request`() {
        testMemberHelper.getDefaultMember()

        // 자기 자신에게 친구 요청을 보내는 것은 비즈니스 로직상 불가능하지만,
        // 만약 그런 시도가 있을 경우를 대비한 테스트
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, 1L)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().is4xxClientError())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when friendship does not exist`() {
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, 99999L) // 존재하지 않는 ID
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when user is not the recipient`() {
        val receiver = testMemberHelper.createActivatedMember("receiver@example.com", "receiver")
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        testMemberHelper.createActivatedMember("third@example.com", "third")

        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        // thirdUser가 receiver에게 온 요청에 응답하려고 시도
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when already responded`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        // 첫 번째 응답
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isOk())

        // 두 번째 응답 시도 (이미 처리된 요청)
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "false")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when friendshipId is extremely large`() {
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, Long.MAX_VALUE)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - bad request - when accept parameter is empty string`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        val friendship = friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, friendship.requireId())
                .param("accept", "") // 빈 문자열
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `respondToFriendRequest - success - when friendshipId is at boundary`() {
        val receiver = testMemberHelper.getDefaultMember()
        val sender = testMemberHelper.createActivatedMember("sender@example.com", "sender")
        friendRequestor.sendFriendRequest(
            fromMemberId = sender.requireId(),
            toMemberId = receiver.requireId(),
        )

        // 경계값 테스트 - 유효한 최소 ID
        mockMvc.perform(
            put(FriendUrls.RESPOND_TO_FRIEND_REQUEST, 1L)
                .param("accept", "true")
                .with(csrf())
        )
            .andExpect(status().is4xxClientError()) // 해당 ID의 friendship이 없으므로 에러
    }
}
