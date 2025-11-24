package com.albert.realmoneyrealtaste.adapter.webapi.comment

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.provided.CommentCreator
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestPostHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CommentWriteApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var testPostHelper: TestPostHelper

    @Autowired
    private lateinit var commentCreator: CommentCreator

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `createComment - success - creates root comment without parentCommentId`() {
        val loginMember = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .param("content", "새로운 댓글")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content.text").value("새로운 댓글"))
            .andExpect(jsonPath("$.postId").value(post.requireId()))
            .andExpect(jsonPath("$.author.memberId").value(loginMember.requireId()))
            .andExpect(jsonPath("$.parentCommentId").doesNotExist())
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `createComment - success - creates reply with parentCommentId`() {
        val loginMember = testMemberHelper.getDefaultMember()
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 먼저 부모 댓글 생성
        val parentComment = commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "부모 댓글",
                memberId = author.requireId()
            )
        )

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .param("content", "답글 내용")
                .param("parentCommentId", parentComment.requireId().toString())
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content.text").value("답글 내용"))
            .andExpect(jsonPath("$.postId").value(post.requireId()))
            .andExpect(jsonPath("$.author.memberId").value(loginMember.requireId()))
            .andExpect(jsonPath("$.parentCommentId").value(parentComment.requireId()))
    }

    @Test
    fun `createComment - forbidden - when not authenticated`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .param("content", "인증 없는 댓글")
                .with(csrf())
        )
            .andExpect(status().isForbidden())
    }

    @WithMockMember
    @Test
    fun `createComment - failure - when post does not exist`() {
        mockMvc.perform(
            post("/api/posts/9999/comments")
                .param("content", "존재하지 않는 게시물 댓글")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `createComment - failure - when parent comment does not exist`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .param("content", "존재하지 않는 부모 댓글에 대한 답글")
                .param("parentCommentId", "9999")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `createComment - failure - when content is empty`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .param("content", "")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `createComment - failure - when content is missing`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            post("/api/posts/${post.requireId()}/comments")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    @Test
    fun `updateComment - success - updates own comment`() {
        val author = testMemberHelper.getDefaultMember()
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 댓글 생성
        val comment = commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "원래 댓글",
                memberId = author.requireId()
            )
        )

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/${comment.requireId()}")
                .param("content", "수정된 댓글")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.content.text").value("수정된 댓글"))
            .andExpect(jsonPath("$.id").value(comment.requireId()))
            .andExpect(jsonPath("$.postId").value(post.requireId()))
            .andExpect(jsonPath("$.author.memberId").value(author.requireId()))
    }

    @Test
    fun `updateComment - forbidden - when not authenticated`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/1")
                .param("content", "인증 없는 수정")
                .with(csrf())
        )
            .andExpect(status().isForbidden())
    }

    @WithMockMember
    @Test
    fun `updateComment - failure - when trying to update other's comment`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val otherUser = testMemberHelper.createActivatedMember("other@example.com", "other")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 다른 사용자의 댓글 생성
        val comment = commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "다른 사람 댓글",
                memberId = otherUser.requireId()
            )
        )

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/${comment.requireId()}")
                .param("content", "남의 댓글 수정 시도")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `updateComment - failure - when comment does not exist`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/9999")
                .param("content", "존재하지 않는 댓글 수정")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `updateComment - failure - when post does not exist`() {
        mockMvc.perform(
            put("/api/posts/9999/comments/1")
                .param("content", "존재하지 않는 게시물의 댓글 수정")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `updateComment - failure - when content is empty`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 댓글 생성
        val comment = commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "원래 댓글",
                memberId = author.requireId()
            )
        )

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/${comment.requireId()}")
                .param("content", "")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }

    @WithMockMember
    @Test
    fun `updateComment - failure - when content is missing`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(
            put("/api/posts/${post.requireId()}/comments/1")
                .with(csrf())
        )
            .andExpect(status().isBadRequest())
    }
}
