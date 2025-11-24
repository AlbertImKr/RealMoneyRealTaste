package com.albert.realmoneyrealtaste.adapter.webapi.friend

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.friend.dto.FriendResponseRequest
import com.albert.realmoneyrealtaste.application.friend.provided.FriendRequestor
import com.albert.realmoneyrealtaste.application.friend.provided.FriendResponder
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FriendReadApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var friendRequestor: FriendRequestor

    @Autowired
    private lateinit var friendResponder: FriendResponder

    @WithMockMember
    @Test
    fun `getFriends - success - returns all friends for existing member`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val friend1 = testMemberHelper.createActivatedMember("friend1@example.com", "friend1")
        val friend2 = testMemberHelper.createActivatedMember("friend2@example.com", "friend2")

        // 친구 관계 설정 (targetMember가 친구 요청을 보내고 수락받음)
        val friendRequest1 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend1.requireId(), friend1.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest1.requireId(), friend1.requireId(), true)
        )

        val friendRequest2 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend2.requireId(), friend2.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest2.requireId(), friend2.requireId(), true)
        )

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(10)) // 기본 페이지 크기
    }

    @WithMockMember
    @Test
    fun `getFriends - success - returns empty list for member with no friends`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0))
    }

    @WithMockMember
    @Test
    fun `getFriends - success - returns paginated results`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 여러 친구 생성
        val friends = mutableListOf<Long>()
        repeat(5) { index ->
            val friend = testMemberHelper.createActivatedMember("friend$index@example.com", "friend$index")
            val friendRequest = friendRequestor.sendFriendRequest(
                FriendRequestCommand(targetMember.requireId(), friend.requireId(), friend.nickname.value)
            )
            friendResponder.respondToFriendRequest(
                FriendResponseRequest(friendRequest.requireId(), friend.requireId(), true)
            )
            friends.add(friend.requireId())
        }

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends?page=0&size=3"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.size").value(3))
    }

    @WithMockMember
    @Test
    fun `getFriends - success - returns sorted results by createdAt DESC`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val friend1 = testMemberHelper.createActivatedMember("friend1@example.com", "friend1")
        val friend2 = testMemberHelper.createActivatedMember("friend2@example.com", "friend2")

        // 먼저 friend1과 친구 관계 설정
        val friendRequest1 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend1.requireId(), friend1.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest1.requireId(), friend1.requireId(), true)
        )

        // 잠시 후 friend2와 친구 관계 설정
        Thread.sleep(100) // 시간 차이를 위해
        val friendRequest2 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend2.requireId(), friend2.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest2.requireId(), friend2.requireId(), true)
        )

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            // 최신 친구인 friend2가 먼저 나와야 함
            .andExpect(jsonPath("$.content[0].friendMemberId").value(friend2.requireId()))
            .andExpect(jsonPath("$.content[1].friendMemberId").value(friend1.requireId()))
    }

    @WithMockMember
    @Test
    fun `getFriends - success - returns empty list for non-existent member`() {
        mockMvc.perform(get("/api/members/9999/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
    }

    @WithMockMember
    @Test
    fun `getFriendsCount - success - returns correct count for member with friends`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val friend1 = testMemberHelper.createActivatedMember("friend1@example.com", "friend1")
        val friend2 = testMemberHelper.createActivatedMember("friend2@example.com", "friend2")

        // 친구 관계 설정
        val friendRequest1 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend1.requireId(), friend1.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest1.requireId(), friend1.requireId(), true)
        )

        val friendRequest2 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend2.requireId(), friend2.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest2.requireId(), friend2.requireId(), true)
        )

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(2))
    }

    @WithMockMember
    @Test
    fun `getFriendsCount - success - returns zero for member with no friends`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(0))
    }

    @WithMockMember
    @Test
    fun `getFriendsCount - success - returns zero for non-existent member`() {
        mockMvc.perform(get("/api/members/9999/friends/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(0))
    }

    @WithMockMember
    @Test
    fun `getFriends - success - excludes pending friend requests`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val friend1 = testMemberHelper.createActivatedMember("friend1@example.com", "friend1")
        val friend2 = testMemberHelper.createActivatedMember("friend2@example.com", "friend2")

        // friend1과는 친구 관계 완료
        val friendRequest1 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend1.requireId(), friend1.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest1.requireId(), friend1.requireId(), true)
        )

        // friend2에게는 친구 요청만 보내고 수락받지 않음
        friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend2.requireId(), friend2.nickname.value)
        )

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1)) // 수락된 친구만 포함
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].friendMemberId").value(friend1.requireId()))
    }

    @WithMockMember
    @Test
    fun `getFriends - success - excludes rejected friend requests`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")
        val friend1 = testMemberHelper.createActivatedMember("friend1@example.com", "friend1")
        val friend2 = testMemberHelper.createActivatedMember("friend2@example.com", "friend2")

        // friend1과는 친구 관계 완료
        val friendRequest1 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend1.requireId(), friend1.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest1.requireId(), friend1.requireId(), true)
        )

        // friend2의 요청은 거절됨
        val friendRequest2 = friendRequestor.sendFriendRequest(
            FriendRequestCommand(targetMember.requireId(), friend2.requireId(), friend2.nickname.value)
        )
        friendResponder.respondToFriendRequest(
            FriendResponseRequest(friendRequest2.requireId(), friend2.requireId(), false)
        )

        mockMvc.perform(get("/api/members/${targetMember.requireId()}/friends"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1)) // 수락된 친구만 포함
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].friendMemberId").value(friend1.requireId()))
    }
}
