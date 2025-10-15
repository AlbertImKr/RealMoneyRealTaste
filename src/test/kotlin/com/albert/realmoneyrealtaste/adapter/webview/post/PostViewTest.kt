package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

class PostViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var postRepository: PostRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - success - creates post and redirects to home`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "정말 맛있는 맛집입니다!")
                .param("contentRating", "5")
                .param("imagesUrls", "https://example.com/image1.jpg", "https://example.com/image2.jpg")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
    }

    @Test
    fun `createPost - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "정말 맛있는 맛집입니다!")
                .param("contentRating", "5")
                .param("imagesUrls", "https://example.com/image1.jpg", "https://example.com/image2.jpg")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when required fields are missing`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("content", "내용만 있음")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when rating is out of range`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "짧")
                .param("contentRating", "66")
                .param("imagesUrls", "https://example.com/image1.jpg")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPost - success - returns post detail view with post information`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostView.POST_DETAIL_VIEW_NAME))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attributeExists("currentUserId"))
    }

    @Test
    fun `readPost - failure - returns forbidden when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPost - failure - returns error when post not found`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            get("/posts/{postId}", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPost - success - increments view count for different user`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        testMemberHelper.createActivatedMember() // viewer
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostView.POST_DETAIL_VIEW_NAME))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `editPost GET - success - returns edit view with post edit form`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}/edit", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("post/edit"))
            .andExpect(model().attributeExists("postEditForm"))
    }

    @Test
    fun `editPost GET - failure - returns forbidden when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}/edit", post.requireId())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `editPost GET - failure - returns error when post not found`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            get("/posts/{postId}/edit", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(memberId = 2, email = "other@example.com")
    fun `editPost GET - failure - returns error when user is not author`() {
        val author = testMemberHelper.createActivatedMember()
        testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}/edit", post.requireId())
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - success - updates post and redirects to post detail`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("id", post.requireId().toString())
                .param("restaurantName", "수정된 맛집")
                .param("restaurantAddress", "수정된 주소")
                .param("restaurantLatitude", "37.1234")
                .param("restaurantLongitude", "127.5678")
                .param("contentText", "수정된 내용")
                .param("contentRating", "4")
                .param("imagesUrls", "https://example.com/new1.jpg")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/posts/${post.requireId()}"))
    }

    @Test
    fun `updatePost POST - failure - returns forbidden when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "수정 시도")
                .param("rating", "4")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - validation error when content is blank`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "")
                .param("rating", "4")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = "other@example.com")
    fun `updatePost POST - failure - returns error when user is not author`() {
        val author = testMemberHelper.createActivatedMember()
        testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "수정 시도")
                .param("rating", "4")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - validation error when rating is negative`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "내용")
                .param("rating", "-1")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - not allowed to set more than 5 images`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 URL이 빈 값임")
                .param("contentRating", "4")
                .param(
                    "imagesUrls",
                    "https://example.com/1.jpg",
                    "https://example.com/2.jpg",
                    "https://example.com/3.jpg",
                    "https://example.com/4.jpg",
                    "https://example.com/5.jpg",
                    "https://example.com/6.jpg"
                )
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when images is empty URLs`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 URL이 빈 값임")
                .param("contentRating", "4")
                .param("imagesUrls", "")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when images exceed maximum count`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 URL이 빈 값임")
                .param("contentRating", "4")
                .param(
                    "imagesUrls",
                    "https://example.com/1.jpg",
                    "https://example.com/2.jpg",
                    "https://example.com/3.jpg",
                    "https://example.com/4.jpg",
                    "https://example.com/5.jpg",
                    "https://example.com/6.jpg"
                )
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure -  - creates post without images`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 없이 작성")
                .param("contentRating", "4")
        )
            .andExpect(status().is4xxClientError())
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPost - success - author reads their own post`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostView.POST_DETAIL_VIEW_NAME))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attributeExists("currentUserId"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - validation error when content exceeds maximum length`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "a".repeat(1001))
                .param("rating", "4")
        )
            .andExpect(status().is4xxClientError)
    }
}
