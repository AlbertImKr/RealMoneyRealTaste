package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.application.post.required.PostRepository
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import com.albert.realmoneyrealtaste.util.MemberFixture
import com.albert.realmoneyrealtaste.util.PostFixture
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.WithMockMember
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

class CommentWriteViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - success - creates new comment and returns comment fragment`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "새로운 댓글입니다")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/comments-fragment :: replies-list"))
            .andExpect(model().attributeExists("comment"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - success - creates reply and returns reply fragment`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val parentComment = createAndSaveComment(post.requireId(), "부모 댓글", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "대댓글입니다")
                .param("parentCommentId", parentComment.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/replies-fragment :: comments-list"))
            .andExpect(model().attributeExists("comment"))
    }

    @Test
    fun `createComment - failure - returns forbidden when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "인증되지 않은 댓글")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - failure - returns error when post not found`() {
        testMemberHelper.createActivatedMember()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", "99999")
                .param("content", "존재하지 않는 게시글에 댓글")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - failure - returns error when content is blank`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - failure - returns error when parent comment not found`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/comments")
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "존재하지 않는 부모 댓글에 대댓글")
                .param("parentCommentId", "99999")
        )
            .andExpect(status().is4xxClientError)
    }

    // Helper method for creating test data

    private fun createAndSaveComment(postId: Long, text: String, authorMemberId: Long): Comment {
        val comment = Comment.create(
            postId = postId,
            authorMemberId = authorMemberId,
            authorNickname = "테스트유저$authorMemberId",
            content = CommentContent(text)
        )
        return commentRepository.save(comment)
    }
}
