package com.albert.realmoneyrealtaste.adapter.webapi.comment

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.provided.CommentCreator
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestPostHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CommentReadApiTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var testPostHelper: TestPostHelper

    @Autowired
    private lateinit var commentCreator: CommentCreator

    @WithMockMember
    @Test
    fun `getCommentCount - success - returns correct count for post with comments`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 댓글 생성
        repeat(3) { index ->
            commentCreator.createComment(
                CommentCreateRequest(
                    postId = post.requireId(),
                    content = "댓글 내용 $index",
                    memberId = author.requireId()
                )
            )
        }

        mockMvc.perform(get("/api/posts/${post.requireId()}/comments/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(3))
    }

    @WithMockMember
    @Test
    fun `getCommentCount - success - returns zero for post with no comments`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        mockMvc.perform(get("/api/posts/${post.requireId()}/comments/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(0))
    }

    @WithMockMember
    @Test
    fun `getCommentCount - success - returns zero for non-existent post`() {
        mockMvc.perform(get("/api/posts/9999/comments/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(0))
    }

    @WithMockMember
    @Test
    fun `getCommentCount - success - counts only root comments (excludes replies)`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post = testPostHelper.createPost(author.requireId(), "테스트 게시물")

        // 루트 댓글 생성
        val rootComment1 = commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "루트 댓글 1",
                memberId = author.requireId()
            )
        )

        commentCreator.createComment(
            CommentCreateRequest(
                postId = post.requireId(),
                content = "루트 댓글 2",
                memberId = author.requireId()
            )
        )

        // 답글 생성 (카운트에서 제외되어야 함)
        repeat(2) { index ->
            commentCreator.createReply(
                com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest(
                    parentCommentId = rootComment1.requireId(),
                    content = "답글 내용 $index",
                    memberId = author.requireId(),
                    postId = post.requireId()
                )
            )
        }

        mockMvc.perform(get("/api/posts/${post.requireId()}/comments/count"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").value(2)) // 답글은 제외된 루트 댓글만 카운트
    }

    @WithMockMember
    @Test
    fun `getCommentCount - success - handles multiple posts independently`() {
        val author = testMemberHelper.createActivatedMember("author@example.com", "author")
        val post1 = testPostHelper.createPost(author.requireId(), "게시물 1")
        val post2 = testPostHelper.createPost(author.requireId(), "게시물 2")

        // post1에 댓글 3개
        repeat(3) { index ->
            commentCreator.createComment(
                CommentCreateRequest(
                    postId = post1.requireId(),
                    content = "게시물 1 댓글 $index",
                    memberId = author.requireId()
                )
            )
        }

        // post2에 댓글 2개
        repeat(2) { index ->
            commentCreator.createComment(
                CommentCreateRequest(
                    postId = post2.requireId(),
                    content = "게시물 2 댓글 $index",
                    memberId = author.requireId()
                )
            )
        }

        // 각 게시물의 댓글 수 확인
        mockMvc.perform(get("/api/posts/${post1.requireId()}/comments/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(3))

        mockMvc.perform(get("/api/posts/${post2.requireId()}/comments/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(2))
    }
}
