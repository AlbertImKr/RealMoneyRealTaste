package com.albert.realmoneyrealtaste.adapter.webview.comment

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import kotlin.test.Test

class CommentReadViewTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Test
    fun `getCommentsFragment - success - returns comments fragment with comments data`() {
        val postId = 1L
        createAndSaveComment(postId, "첫 번째 댓글")
        createAndSaveComment(postId, "두 번째 댓글")
        flushAndClear()

        mockMvc.perform(
            get("/comments/fragments/list")
                .param("postId", postId.toString())
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/comments-fragment :: comments-list"))
            .andExpect(model().attributeExists("comments"))
            .andExpect(model().attributeExists("postId"))
            .andExpect(model().attribute("postId", postId))
    }

    @Test
    fun `getRepliesFragment - success - returns replies fragment with replies data`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveReply(postId, parentCommentId, "첫 번째 대댓글")
        createAndSaveReply(postId, parentCommentId, "두 번째 대댓글")
        flushAndClear()

        mockMvc.perform(
            get("/comments/{commentId}/replies/fragments/list", parentCommentId)
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/replies-fragment :: replies-list"))
            .andExpect(model().attributeExists("replies"))
            .andExpect(model().attributeExists("commentId"))
            .andExpect(model().attribute("commentId", parentCommentId))
    }

    @Test
    fun `getModalCommentsFragment - success - returns modal comments fragment`() {
        val postId = 1L
        createAndSaveComment(postId, "모달용 댓글")
        flushAndClear()

        mockMvc.perform(
            get("/comments/modal-fragments/list")
                .param("postId", postId.toString())
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/modal-comments :: modal-comments-list"))
            .andExpect(model().attributeExists("comments"))
            .andExpect(model().attributeExists("postId"))
    }

    @Test
    fun `getModalRepliesFragment - success - returns modal replies fragment`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveReply(postId, parentCommentId, "모달용 대댓글")
        flushAndClear()

        mockMvc.perform(
            get("/comments/modal-replies-fragment")
                .param("commentId", parentCommentId.toString())
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/modal-comments :: modal-replies-list"))
            .andExpect(model().attributeExists("replies"))
            .andExpect(model().attributeExists("commentId"))
    }

    private fun createAndSaveComment(postId: Long, text: String, authorMemberId: Long = 100L): Comment {
        val comment = Comment.create(
            postId = postId,
            authorMemberId = authorMemberId,
            authorNickname = "테스트유저$authorMemberId",
            content = CommentContent(text)
        )
        return commentRepository.save(comment)
    }

    private fun createAndSaveReply(
        postId: Long,
        parentCommentId: Long,
        text: String,
        authorMemberId: Long = 200L,
    ): Comment {
        val reply = Comment.create(
            postId = postId,
            authorMemberId = authorMemberId,
            authorNickname = "대댓글유저$authorMemberId",
            content = CommentContent(text),
            parentCommentId = parentCommentId
        )
        return commentRepository.save(reply)
    }
}
