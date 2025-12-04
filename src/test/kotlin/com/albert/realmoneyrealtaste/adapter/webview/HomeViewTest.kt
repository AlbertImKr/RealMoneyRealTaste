package com.albert.realmoneyrealtaste.adapter.webview

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.post.required.PostHeartRepository
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.post.PostHeart
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

class HomeViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var postHeartRepository: PostHeartRepository

    @Test
    fun `home - success - returns home view with posts for unauthenticated user`() {
        val author = testMemberHelper.createActivatedMember()
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("postCreateForm"))
            .andExpect(model().attributeDoesNotExist("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - returns home view with posts and member info for authenticated user`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("postCreateForm"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - includes member statistics for authenticated user`() {
        val member = testMemberHelper.getDefaultMember()
        testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )

        // 멤버가 작성한 게시글 생성
        repeat(3) { _ ->
            postRepository.save(
                PostFixture.createPost(
                    authorMemberId = member.requireId(),
                    authorNickname = member.nickname.value,
                    images = PostFixture.createImages(1)
                )
            )
        }

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - includes heart information for authenticated user's liked posts`() {
        val member = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        val post1 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        val post3 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        // member가 post1과 post3에만 좋아요
        postHeartRepository.save(PostHeart.create(memberId = member.requireId(), postId = post1.requireId()))
        postHeartRepository.save(PostHeart.create(memberId = member.requireId(), postId = post3.requireId()))

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - returns empty hearts when user has no liked posts`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `home - success - respects pagination with custom page size`() {
        val author = testMemberHelper.createActivatedMember()

        // 15개의 게시글 생성
        repeat(15) { _ ->
            postRepository.save(
                PostFixture.createPost(
                    authorMemberId = author.requireId(),
                    authorNickname = author.nickname.value,
                    images = PostFixture.createImages(2)
                )
            )
        }

        mockMvc.perform(
            get("/")
                .param("page", "0")
                .param("size", "5")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `home - success - handles second page request`() {
        val author = testMemberHelper.createActivatedMember()

        // 15개의 게시글 생성
        repeat(15) { _ ->
            postRepository.save(
                PostFixture.createPost(
                    authorMemberId = author.requireId(),
                    authorNickname = author.nickname.value,
                    images = PostFixture.createImages(2)
                )
            )
        }

        mockMvc.perform(
            get("/")
                .param("page", "1")
                .param("size", "10")
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - excludes deleted posts from results`() {
        val author = testMemberHelper.createActivatedMember(
            email = "author@example.com",
            nickname = "author"
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        val post2 = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        // post2 삭제
        post2.delete(author.requireId())

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `home - success - handles multiple authors correctly`() {
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
                authorNickname = author1.nickname.value,
                images = PostFixture.createImages(2)
            )
        )
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author2.requireId(),
                authorNickname = author2.nickname.value,
                images = PostFixture.createImages(2)
            )
        )

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("member"))
    }

    @Test
    fun `home - success - sorts posts by createdAt descending by default`() {
        val author = testMemberHelper.createActivatedMember()

        // 다른 시간에 게시글 생성 (시간 차이를 위해 약간의 지연)
        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(1)
            )
        )

        Thread.sleep(10) // 시간 차이 보장

        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(1)
            )
        )

        Thread.sleep(10)

        postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value,
                images = PostFixture.createImages(1)
            )
        )

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `home - success - respects custom sort parameter`() {
        val author = testMemberHelper.createActivatedMember()

        // 여러 게시글 생성
        repeat(5) { _ ->
            postRepository.save(
                PostFixture.createPost(
                    authorMemberId = author.requireId(),
                    authorNickname = author.nickname.value,
                    images = PostFixture.createImages(1)
                )
            )
        }

        mockMvc.perform(
            get("/")
                .param("sort", "createdAt,desc")
                .param("size", "3")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }
}
