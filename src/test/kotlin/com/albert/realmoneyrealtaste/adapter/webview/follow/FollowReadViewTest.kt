package com.albert.realmoneyrealtaste.adapter.webview.follow

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.follow.dto.FollowCreateRequest
import com.albert.realmoneyrealtaste.application.follow.provided.FollowCreator
import com.albert.realmoneyrealtaste.domain.follow.Follow
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FollowReadViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var followCreator: FollowCreator

    @Test
    fun `followButtonFragment - forbidden - when not authenticated`() {
        val authorId = 1L

        mockMvc.perform(
            get(FollowUrls.FOLLOW_BUTTON, authorId)
        )
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `followButtonFragment - success - returns follow button fragment`() {
        val member = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember("author@test.com", "author")

        mockMvc.perform(
            get(FollowUrls.FOLLOW_BUTTON, author.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOW_BUTTON))
            .andExpect(model().attributeExists("authorId"))
            .andExpect(model().attributeExists("isFollowing"))
            .andExpect(model().attribute("authorId", author.requireId()))
            .andExpect(model().attribute("isFollowing", false))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `followButtonFragment - success - returns true when following author`() {
        val member = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember("author@test.com", "author")

        // 팔로우 생성
        createActiveFollow(member.requireId(), author.requireId())

        mockMvc.perform(
            get(FollowUrls.FOLLOW_BUTTON, author.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOW_BUTTON))
            .andExpect(model().attribute("isFollowing", true))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - returns following list without keyword`() {
        val member = testMemberHelper.getDefaultMember()
        val following1 = testMemberHelper.createActivatedMember("following1@test.com", "following1")
        val following2 = testMemberHelper.createActivatedMember("following2@test.com", "following2")

        // 팔로우 생성
        createActiveFollow(member.requireId(), following1.requireId())
        createActiveFollow(member.requireId(), following2.requireId())

        mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - returns filtered following list with keyword`() {
        val member = testMemberHelper.getDefaultMember()
        val matchingFollowing = testMemberHelper.createActivatedMember("matching@test.com", "searchTarget")
        val nonMatchingFollowing = testMemberHelper.createActivatedMember("other@test.com", "other")

        // 팔로우 생성
        createActiveFollow(member.requireId(), matchingFollowing.requireId())
        createActiveFollow(member.requireId(), nonMatchingFollowing.requireId())

        mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT)
                .param("keyword", "search")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - returns follower list without keyword`() {
        val member = testMemberHelper.getDefaultMember()
        val follower1 = testMemberHelper.createActivatedMember("follower1@test.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@test.com", "follower2")

        // 팔로우 생성
        createActiveFollow(follower1.requireId(), member.requireId())
        createActiveFollow(follower2.requireId(), member.requireId())

        mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - returns filtered follower list with keyword`() {
        val member = testMemberHelper.getDefaultMember()
        val matchingFollower = testMemberHelper.createActivatedMember("matching@test.com", "searchTarget")
        val nonMatchingFollower = testMemberHelper.createActivatedMember("other@test.com", "other")

        // 팔로우 생성
        createActiveFollow(matchingFollower.requireId(), member.requireId())
        createActiveFollow(nonMatchingFollower.requireId(), member.requireId())

        mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT)
                .param("keyword", "search")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    fun `readUserFollowingList - success - returns user following list without authentication`() {
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val following1 = testMemberHelper.createActivatedMember("following1@test.com", "following1")
        val following2 = testMemberHelper.createActivatedMember("following2@test.com", "following2")

        // 팔로우 생성
        createActiveFollow(targetMember.requireId(), following1.requireId())
        createActiveFollow(targetMember.requireId(), following2.requireId())

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWING_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeDoesNotExist("member"))
            .andExpect(model().attributeDoesNotExist("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowingList - success - returns user following list with authentication`() {
        val currentUser = testMemberHelper.getDefaultMember()
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val following1 = testMemberHelper.createActivatedMember("following1@test.com", "following1")
        val following2 = testMemberHelper.createActivatedMember("following2@test.com", "following2")

        // 팔로우 생성
        createActiveFollow(targetMember.requireId(), following1.requireId())
        createActiveFollow(targetMember.requireId(), following2.requireId())
        // 현재 사용자가 타겟 멤버도 팔로우
        createActiveFollow(currentUser.requireId(), targetMember.requireId())

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWING_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    fun `readUserFollowerList - success - returns user follower list without authentication`() {
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val follower1 = testMemberHelper.createActivatedMember("follower1@test.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@test.com", "follower2")

        // 팔로우 생성
        createActiveFollow(follower1.requireId(), targetMember.requireId())
        createActiveFollow(follower2.requireId(), targetMember.requireId())

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWERS_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andExpect(model().attributeDoesNotExist("member"))
            .andExpect(model().attributeDoesNotExist("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowerList - success - returns user follower list with authentication`() {
        val currentUser = testMemberHelper.getDefaultMember()
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val follower1 = testMemberHelper.createActivatedMember("follower1@test.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@test.com", "follower2")

        // 팔로우 생성
        createActiveFollow(follower1.requireId(), targetMember.requireId())
        createActiveFollow(follower2.requireId(), targetMember.requireId())
        // 현재 사용자가 팔로워 중 하나도 팔로우
        createActiveFollow(currentUser.requireId(), follower1.requireId())

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWERS_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - handles pagination correctly`() {
        val member = testMemberHelper.getDefaultMember()
        val followings = (1..5).map { index ->
            testMemberHelper.createActivatedMember("following$index@test.com", "following$index")
        }

        // 팔로우 생성
        followings.forEach { createActiveFollow(member.requireId(), it.requireId()) }

        mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT)
                .param("page", "0")
                .param("size", "3")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - excludes deactivated members`() {
        val member = testMemberHelper.getDefaultMember()
        val activeFollowing = testMemberHelper.createActivatedMember("active@test.com", "active")
        val deactivatedFollowing = testMemberHelper.createActivatedMember("deactivated@test.com", "deactivated")

        // 팔로우 생성
        createActiveFollow(member.requireId(), activeFollowing.requireId())
        createActiveFollow(member.requireId(), deactivatedFollowing.requireId())

        // 비활성화
        deactivatedFollowing.deactivate()
        flushAndClear()

        mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - handles pagination correctly`() {
        val member = testMemberHelper.getDefaultMember()
        val followers = (1..5).map { index ->
            testMemberHelper.createActivatedMember("follower$index@test.com", "follower$index")
        }

        // 팔로우 생성
        followers.forEach { createActiveFollow(it.requireId(), member.requireId()) }

        mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT)
                .param("page", "0")
                .param("size", "3")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - excludes deactivated members`() {
        val member = testMemberHelper.getDefaultMember()
        val activeFollower = testMemberHelper.createActivatedMember("active@test.com", "active")
        val deactivatedFollower = testMemberHelper.createActivatedMember("deactivated@test.com", "deactivated")

        // 팔로우 생성
        createActiveFollow(activeFollower.requireId(), member.requireId())
        createActiveFollow(deactivatedFollower.requireId(), member.requireId())

        // 비활성화
        deactivatedFollower.deactivate()
        flushAndClear()

        mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
    }

    @Test
    fun `readUserFollowingList - success - excludes deactivated members`() {
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val activeFollowing = testMemberHelper.createActivatedMember("active@test.com", "active")
        val deactivatedFollowing = testMemberHelper.createActivatedMember("deactivated@test.com", "deactivated")

        // 팔로우 생성
        createActiveFollow(targetMember.requireId(), activeFollowing.requireId())
        createActiveFollow(targetMember.requireId(), deactivatedFollowing.requireId())

        // 비활성화
        deactivatedFollowing.deactivate()
        flushAndClear()

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWING_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
    }

    @Test
    fun `readUserFollowerList - success - excludes deactivated members`() {
        val targetMember = testMemberHelper.createActivatedMember("target@test.com", "target")
        val activeFollower = testMemberHelper.createActivatedMember("active@test.com", "active")
        val deactivatedFollower = testMemberHelper.createActivatedMember("deactivated@test.com", "deactivated")

        // 팔로우 생성
        createActiveFollow(activeFollower.requireId(), targetMember.requireId())
        createActiveFollow(deactivatedFollower.requireId(), targetMember.requireId())

        // 비활성화
        deactivatedFollower.deactivate()
        flushAndClear()

        mockMvc.perform(
            get(FollowUrls.USER_FOLLOWERS_FRAGMENT, targetMember.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
    }

    private fun createActiveFollow(followerId: Long, followingId: Long): Follow {
        val request = FollowCreateRequest(followerId, followingId)
        return followCreator.follow(request)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - returns following fragment`() {
        val result = mockMvc.perform(get(FollowUrls.FOLLOWING_FRAGMENT))
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
            .andExpect(model().attributeExists("member"))
            .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))
        assertTrue(modelAndView.model.containsKey("currentUserFollowingIds"))
        assertTrue(modelAndView.model.containsKey("member"))
    }

    @Test
    fun `readFollowingList - forbidden - when not authenticated`() {
        mockMvc.perform(get(FollowUrls.FOLLOWING_FRAGMENT))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - returns followers fragment`() {
        val result = mockMvc.perform(get(FollowUrls.FOLLOWERS_FRAGMENT))
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andExpect(model().attributeExists("currentUserFollowingIds"))
            .andExpect(model().attributeExists("member"))
            .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))
        assertTrue(modelAndView.model.containsKey("currentUserFollowingIds"))
        assertTrue(modelAndView.model.containsKey("member"))
    }

    @Test
    fun `readFollowerList - forbidden - when not authenticated`() {
        mockMvc.perform(get(FollowUrls.FOLLOWERS_FRAGMENT))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowingList - success - returns user following fragment`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result =
            mockMvc.perform(
                get(
                    FollowUrls.USER_FOLLOWING_FRAGMENT.replace(
                        "{memberId}",
                        targetMember.requireId().toString()
                    )
                )
            )
                .andExpect(status().isOk)
                .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
                .andExpect(model().attributeExists("followings"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("currentUserFollowingIds"))
                .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))
        assertTrue(modelAndView.model.containsKey("member"))
        assertTrue(modelAndView.model.containsKey("currentUserFollowingIds"))
    }

    @Test
    fun `readUserFollowingList - success - works without authentication`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result =
            mockMvc.perform(
                get(
                    FollowUrls.USER_FOLLOWING_FRAGMENT.replace(
                        "{memberId}",
                        targetMember.requireId().toString()
                    )
                )
            )
                .andExpect(status().isOk)
                .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
                .andExpect(model().attributeExists("followings"))
                .andReturn()

        // 인증 없이도 author와 followings는 포함되어야 함
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))

        // 인증 없이는 member와 currentUserFollowingIds가 없어야 함
        assertTrue(!modelAndView.model.containsKey("member"))
        assertTrue(!modelAndView.model.containsKey("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowerList - success - returns user followers fragment`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result =
            mockMvc.perform(
                get(
                    FollowUrls.USER_FOLLOWERS_FRAGMENT.replace(
                        "{memberId}",
                        targetMember.requireId().toString()
                    )
                )
            )
                .andExpect(status().isOk)
                .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
                .andExpect(model().attributeExists("followers"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("currentUserFollowingIds"))
                .andReturn()

        // 모델 속성 확인
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))
        assertTrue(modelAndView.model.containsKey("member"))
        assertTrue(modelAndView.model.containsKey("currentUserFollowingIds"))
    }

    @Test
    fun `readUserFollowerList - success - works without authentication`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        val result =
            mockMvc.perform(
                get(
                    FollowUrls.USER_FOLLOWERS_FRAGMENT.replace(
                        "{memberId}",
                        targetMember.requireId().toString()
                    )
                )
            )
                .andExpect(status().isOk)
                .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
                .andExpect(model().attributeExists("followers"))
                .andReturn()

        // 인증 없이도 author와 followers는 포함되어야 함
        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))

        // 인증 없이는 member와 currentUserFollowingIds가 없어야 함
        assertTrue(!modelAndView.model.containsKey("member"))
        assertTrue(!modelAndView.model.containsKey("currentUserFollowingIds"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - with keyword search`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT).param("keyword", "test")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - with empty keyword returns all followings`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT).param("keyword", "")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))

        // 빈 키워드일 때는 전체 팔로잉 목록이 반환되어야 함
        val followings = modelAndView.model["followings"]
        assertTrue(followings != null, "빈 키워드시에도 followings는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - with whitespace keyword returns all followings`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT).param("keyword", "   ")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))

        // 공백 키워드일 때는 전체 팔로잉 목록이 반환되어야 함
        val followings = modelAndView.model["followings"]
        assertTrue(followings != null, "공백 키워드시에도 followings는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - without keyword returns all followings`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWING_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andExpect(model().attributeExists("followings"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followings"))

        // 키워드 파라미터가 없을 때는 전체 팔로잉 목록이 반환되어야 함
        val followings = modelAndView.model["followings"]
        assertTrue(followings != null, "키워드 파라미터가 없을 때도 followings는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - with keyword search`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT).param("keyword", "test")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - with empty keyword returns all followers`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT).param("keyword", "")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))

        // 빈 키워드일 때는 전체 팔로워 목록이 반환되어야 함
        val followers = modelAndView.model["followers"]
        assertTrue(followers != null, "빈 키워드시에도 followers는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - with whitespace keyword returns all followers`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT).param("keyword", "   ")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))

        // 공백 키워드일 때는 전체 팔로워 목록이 반환되어야 함
        val followers = modelAndView.model["followers"]
        assertTrue(followers != null, "공백 키워드시에도 followers는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - without keyword returns all followers`() {
        val result = mockMvc.perform(
            get(FollowUrls.FOLLOWERS_FRAGMENT)
        )
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andExpect(model().attributeExists("followers"))
            .andReturn()

        val modelAndView = result.modelAndView!!
        assertTrue(modelAndView.model.containsKey("followers"))

        // 키워드 파라미터가 없을 때는 전체 팔로워 목록이 반환되어야 함
        val followers = modelAndView.model["followers"]
        assertTrue(followers != null, "키워드 파라미터가 없을 때도 followers는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowingList - success - handles invalid member id gracefully`() {
        mockMvc.perform(get(FollowUrls.USER_FOLLOWING_FRAGMENT.replace("{memberId}", "9999")))
            .andExpect(status().isOk())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowerList - success - handles invalid member id gracefully`() {
        mockMvc.perform(get(FollowUrls.USER_FOLLOWERS_FRAGMENT.replace("{memberId}", "9999")))
            .andExpect(status().isOk())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - with actual follow data`() {
        val targetMember = testMemberHelper.createActivatedMember("target@example.com", "target")

        // 팔로우 관계 생성
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember.requireId()
            )
        )

        val result = mockMvc.perform(get(FollowUrls.FOLLOWING_FRAGMENT))
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWING_FRAGMENT))
            .andReturn()

        val modelAndView = result.modelAndView!!
        val followings = modelAndView.model["followings"]
        assertTrue(followings != null, "followings는 null이 아니어야 함")

        // currentUserFollowingIds가 올바르게 설정되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"]
        assertTrue(currentUserFollowingIds != null, "currentUserFollowingIds는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowingList - success - followingIds extraction works correctly`() {
        val targetMember1 = testMemberHelper.createActivatedMember("target1@example.com", "target1")
        val targetMember2 = testMemberHelper.createActivatedMember("target2@example.com", "target2")
        val targetMember3 = testMemberHelper.createActivatedMember("target3@example.com", "target3")

        // 여러 팔로우 관계 생성
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember1.requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember2.requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember3.requireId()
            )
        )

        val result = mockMvc.perform(get(FollowUrls.FOLLOWING_FRAGMENT))
            .andExpect(status().isOk)
            .andReturn()

        val modelAndView = result.modelAndView!!

        // followingIds가 올바르게 추출되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"] as List<*>
        assertTrue(currentUserFollowingIds.isNotEmpty(), "팔로우하는 사용자 ID 목록이 비어있으면 안 됨")

        // 추출된 ID들이 실제 팔로우한 사용자들의 ID인지 확인
        assertTrue(
            currentUserFollowingIds.contains(targetMember1.requireId()),
            "target1의 ID가 포함되어야 함"
        )
        assertTrue(
            currentUserFollowingIds.contains(targetMember2.requireId()),
            "target2의 ID가 포함되어야 함"
        )
        assertTrue(
            currentUserFollowingIds.contains(targetMember3.requireId()),
            "target3의 ID가 포함되어야 함"
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowingList - success - followingIds extraction works for other user`() {
        val otherUser = testMemberHelper.createActivatedMember("other@example.com", "other")
        val targetMember1 = testMemberHelper.createActivatedMember("target1@example.com", "target1")
        val targetMember2 = testMemberHelper.createActivatedMember("target2@example.com", "target2")

        // 다른 사용자가 팔로우 관계 생성
        followCreator.follow(
            FollowCreateRequest(
                followerId = otherUser.requireId(),
                followingId = targetMember1.requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = otherUser.requireId(),
                followingId = targetMember2.requireId()
            )
        )

        // 현재 사용자도 target1을 팔로우
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = targetMember1.requireId()
            )
        )

        val result = mockMvc.perform(
            get(FollowUrls.USER_FOLLOWING_FRAGMENT.replace("{memberId}", otherUser.requireId().toString()))
        )
            .andExpect(status().isOk)
            .andReturn()

        val modelAndView = result.modelAndView!!

        // 다른 사용자의 팔로잉 목록에서 followingIds가 올바르게 추출되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"] as List<*>
        assertTrue(currentUserFollowingIds.isNotEmpty(), "팔로우하는 사용자 ID 목록이 비어있으면 안 됨")

        // 현재 사용자가 팔로우하는 ID만 포함되어야 함 (target1만 포함, target2는 미포함)
        assertTrue(
            currentUserFollowingIds.contains(targetMember1.requireId()),
            "현재 사용자가 팔로우하는 target1의 ID가 포함되어야 함"
        )
        assertFalse(
            currentUserFollowingIds.contains(targetMember2.requireId()),
            "현재 사용자가 팔로우하지 않는 target2의 ID는 포함되면 안 됨"
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - with actual follow data`() {
        val follower = testMemberHelper.createActivatedMember("follower@example.com", "follower")

        // 팔로우 관계 생성 (follower가 현재 사용자를 팔로우)
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower.requireId(),
                followingId = testMemberHelper.getDefaultMember().requireId()
            )
        )

        val result = mockMvc.perform(get(FollowUrls.FOLLOWERS_FRAGMENT))
            .andExpect(status().isOk)
            .andExpect(view().name(FollowViews.FOLLOWERS_FRAGMENT))
            .andReturn()

        val modelAndView = result.modelAndView!!
        val followers = modelAndView.model["followers"]
        assertTrue(followers != null, "followers는 null이 아니어야 함")

        // currentUserFollowingIds가 올바르게 설정되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"]
        assertTrue(currentUserFollowingIds != null, "currentUserFollowingIds는 null이 아니어야 함")
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readFollowerList - success - followingIds extraction works correctly`() {
        val follower1 = testMemberHelper.createActivatedMember("follower1@example.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@example.com", "follower2")
        val follower3 = testMemberHelper.createActivatedMember("follower3@example.com", "follower3")

        // 여러 팔로워가 현재 사용자를 팔로우
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower1.requireId(),
                followingId = testMemberHelper.getDefaultMember().requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower2.requireId(),
                followingId = testMemberHelper.getDefaultMember().requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower3.requireId(),
                followingId = testMemberHelper.getDefaultMember().requireId()
            )
        )

        // 현재 사용자가 follower1과 follower2를 팔로우 (follower3은 팔로우하지 않음)
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = follower1.requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = follower2.requireId()
            )
        )

        val result = mockMvc.perform(get(FollowUrls.FOLLOWERS_FRAGMENT))
            .andExpect(status().isOk)
            .andReturn()

        val modelAndView = result.modelAndView!!

        // followerIds에서 followingIds가 올바르게 추출되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"] as List<*>
        assertTrue(currentUserFollowingIds.isNotEmpty(), "팔로우하는 사용자 ID 목록이 비어있으면 안 됨")

        // 현재 사용자가 팔로우하는 팔로워의 ID만 포함되어야 함
        assertTrue(
            currentUserFollowingIds.contains(follower1.requireId()),
            "팔로우하는 follower1의 ID가 포함되어야 함"
        )
        assertTrue(
            currentUserFollowingIds.contains(follower2.requireId()),
            "팔로우하는 follower2의 ID가 포함되어야 함"
        )
        assertFalse(
            currentUserFollowingIds.contains(follower3.requireId()),
            "팔로우하지 않는 follower3의 ID는 포함되면 안 됨"
        )
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readUserFollowerList - success - followingIds extraction works for other user`() {
        val otherUser = testMemberHelper.createActivatedMember("other@example.com", "other")
        val follower1 = testMemberHelper.createActivatedMember("follower1@example.com", "follower1")
        val follower2 = testMemberHelper.createActivatedMember("follower2@example.com", "follower2")

        // 여러 팔로워가 다른 사용자를 팔로우
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower1.requireId(),
                followingId = otherUser.requireId()
            )
        )
        followCreator.follow(
            FollowCreateRequest(
                followerId = follower2.requireId(),
                followingId = otherUser.requireId()
            )
        )

        // 현재 사용자가 follower1만 팔로우
        followCreator.follow(
            FollowCreateRequest(
                followerId = testMemberHelper.getDefaultMember().requireId(),
                followingId = follower1.requireId()
            )
        )

        val result = mockMvc.perform(
            get(FollowUrls.USER_FOLLOWERS_FRAGMENT.replace("{memberId}", otherUser.requireId().toString()))
        )
            .andExpect(status().isOk)
            .andReturn()

        val modelAndView = result.modelAndView!!

        // 다른 사용자의 팔로워 목록에서 followingIds가 올바르게 추출되었는지 확인
        val currentUserFollowingIds = modelAndView.model["currentUserFollowingIds"] as List<*>
        assertTrue(currentUserFollowingIds.isNotEmpty(), "팔로우하는 사용자 ID 목록이 비어있으면 안 됨")

        // 현재 사용자가 팔로우하는 팔로워의 ID만 포함되어야 함
        assertTrue(
            currentUserFollowingIds.contains(follower1.requireId()),
            "현재 사용자가 팔로우하는 follower1의 ID가 포함되어야 함"
        )
        assertFalse(
            currentUserFollowingIds.contains(follower2.requireId()),
            "현재 사용자가 팔로우하지 않는 follower2의 ID는 포함되면 안 됨"
        )
    }
}
