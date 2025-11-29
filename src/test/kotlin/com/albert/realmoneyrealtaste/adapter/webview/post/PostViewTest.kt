package com.albert.realmoneyrealtaste.adapter.webview.post

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
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

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - success - creates post and redirects to home`() {
        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "정말 맛있는 맛집입니다!")
                .param("contentRating", "5")
                .param("imageIds", "1", "2")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/posts/**"))
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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.DETAIL))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attributeExists("currentUserId"))
    }

    @Test
    fun `readPost - success - returns post detail view without authentication`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.DETAIL))
            .andExpect(model().attributeExists("post"))
    }

    @Test
    fun `readPost - failure - returns error when post not found without authentication`() {
        mockMvc.perform(
            get("/posts/{postId}", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPost - failure - returns error when post not found`() {
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
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.DETAIL))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `editPost GET - success - returns edit view with post edit form`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

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

        mockMvc.perform(
            get("/posts/{postId}/edit", post.requireId())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `editPost GET - failure - returns error when post not found`() {
        mockMvc.perform(
            get("/posts/{postId}/edit", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `editPost GET - failure - returns error when user is not author`() {
        val author = testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/{postId}/edit", post.requireId())
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - success - updates post and redirects to post detail`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

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
                .param("imageIds", "1")
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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "")
                .param("rating", "4")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - returns error when user is not author`() {
        val author = testMemberHelper.createActivatedMember(
            email = "other@example.com",
            nickname = "other"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

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
    fun `updatePost POST - failure - member is not author`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@email.com",
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 URL이 빈 값임")
                .param("contentRating", "4")
                .param("imageIds", "1")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/posts/${post.requireId()}/edit"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when images is empty URLs`() {
        mockMvc.perform(
            post("/posts/new")
                .with(csrf())
                .param("restaurantName", "테스트 맛집")
                .param("restaurantAddress", "서울시 강남구")
                .param("restaurantLatitude", "37.5665")
                .param("restaurantLongitude", "126.9780")
                .param("contentText", "이미지 URL이 빈 값임")
                .param("contentRating", "4")
                .param("imageIds", "")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createPost - failure - validation error when images exceed maximum count`() {
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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/{postId}", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.DETAIL))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attributeExists("currentUserId"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updatePost POST - failure - validation error when content exceeds maximum length`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            post("/posts/{postId}/edit", post.requireId())
                .with(csrf())
                .param("content", "a".repeat(1001))
                .param("rating", "4")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPostDetailModal - success - returns modal fragment with post information`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/{postId}/modal", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("post/modal-detail :: post-detail-modal"))
            .andExpect(model().attributeExists("post"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPostDetailModal - failure - returns error when post not found`() {
        mockMvc.perform(
            get("/posts/{postId}/modal", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `readPostDetailModal - success - returns modal fragment without authentication`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/{postId}/modal", post.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.DETAIL_MODAL))
            .andExpect(model().attributeExists("post"))
    }

    @Test
    fun `readPostDetailModal - failure - returns error when post not found without authentication`() {
        mockMvc.perform(
            get("/posts/{postId}/modal", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMyPosts - success - returns my posts list view with posts and member info`() {
        val member = testMemberHelper.getDefaultMember()
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/mine")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.MY_LIST))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("member"))
            .andExpect(model().attributeExists("postCreateForm"))
    }

    @Test
    fun `readMyPosts - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            get("/posts/mine")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMemberPostsFragment - success - returns member posts fragment with author and posts`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/members/{id}/posts/fragment", author.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POSTS_CONTENT))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readMemberPostsFragment - success - returns member posts fragment without authentication`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/members/{id}/posts/fragment", author.requireId())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POSTS_CONTENT))
            .andExpect(model().attributeExists("author"))
            .andExpect(model().attributeExists("posts"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMemberPostsFragment - failure - returns error when member not found`() {
        mockMvc.perform(
            get("/members/{id}/posts/fragment", 99999L)
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readMyPostsFragment - success - returns my posts fragment with posts and member info`() {
        val member = testMemberHelper.getDefaultMember()
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/mine/fragment")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POSTS_CONTENT))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readMyPostsFragment - failure - returns forbidden when not authenticated`() {
        mockMvc.perform(
            get("/posts/mine/fragment")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPosts - success - returns posts fragment with posts and member info`() {
        val author1 = testMemberHelper.createActivatedMember(
            email = "author1@example.com",
            nickname = "author1"
        )
        val author2 = testMemberHelper.createActivatedMember(
            email = "author2@example.com",
            nickname = "author2"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author1.requireId(),
                authorNickname = author1.nickname.value
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author2.requireId(),
                authorNickname = author2.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/fragment")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POSTS_CONTENT))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readPosts - success - returns posts fragment without authentication`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )

        mockMvc.perform(
            get("/posts/fragment")
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POSTS_CONTENT))
            .andExpect(model().attributeExists("posts"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPostListFragment - success - returns collection posts fragment with posts and metadata`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        val post1 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        val post2 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        val collectionId = 1L

        mockMvc.perform(
            get("/members/{authorId}/collections/{collectionId}/posts/fragment", author.requireId(), collectionId)
                .param("postIds", post1.requireId().toString())
                .param("postIds", post2.requireId().toString())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POST_LIST_FRAGMENT))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("authorId"))
            .andExpect(model().attributeExists("collectionId"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `readPostListFragment - success - returns collection posts fragment without authentication`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        val collectionId = 1L

        mockMvc.perform(
            get("/members/{authorId}/collections/{collectionId}/posts/fragment", author.requireId(), collectionId)
                .param("postIds", post.requireId().toString())
        )
            .andExpect(status().isOk)
            .andExpect(view().name(PostViews.POST_LIST_FRAGMENT))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attributeExists("authorId"))
            .andExpect(model().attributeExists("collectionId"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `readPostListFragment - failure - returns error when post not found`() {
        val authorId = 1L
        val collectionId = 1L
        val postIds = listOf(99999L)

        mockMvc.perform(
            get("/members/{authorId}/collections/{collectionId}/posts/fragment", authorId, collectionId)
                .param("postIds", objectMapper.writeValueAsString(postIds))
        )
            .andExpect(status().is4xxClientError)
    }
}
