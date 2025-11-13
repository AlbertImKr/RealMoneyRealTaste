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
import kotlin.test.assertEquals

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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/comments", post.requireId())
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
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val parentComment = createAndSaveComment(post.requireId(), "부모 댓글", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/comments", post.requireId())
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
        val member = testMemberHelper.getDefaultMember()
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
        val member = testMemberHelper.getDefaultMember()
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

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - failure - handles content too long`() {
        val post = postRepository.save(PostFixture.createPost(/*...*/))
        val longContent = "a".repeat(501) // CommentContent.MAX_LENGTH 초과

        mockMvc.perform(
            post("/posts/{postId}/comments", post.requireId())
                .with(csrf())
                .param("content", longContent)
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attribute("error", "댓글 내용은 500자를 초과할 수 없습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `createComment - failure - parent comment deleted`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val parentComment = createAndSaveComment(post.requireId(), "부모 댓글", member.requireId())
        // Simulate deletion
        parentComment.delete(member.requireId())
        commentRepository.save(parentComment)
        flushAndClear()

        mockMvc.perform(
            post("/posts/{postId}/comments", post.requireId())
                .with(csrf())
                .param("postId", post.requireId().toString())
                .param("content", "삭제된 부모 댓글에 대댓글")
                .param("parentCommentId", parentComment.id.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attribute("error", "댓글 작성에 실패했습니다."))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - success - updates comment and returns comment fragment`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "원본 댓글 내용", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "수정된 댓글 내용")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/comments-fragment :: comment-item"))
            .andExpect(model().attributeExists("comment"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - success - updates reply and returns reply fragment`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val parentComment = createAndSaveComment(post.requireId(), "부모 댓글", member.requireId())
        val replyComment = createAndSaveReply(post.requireId(), parentComment.id!!, "원본 대댓글", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", replyComment.id)
                .with(csrf())
                .param("content", "수정된 대댓글 내용")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/replies-fragment :: reply-item"))
            .andExpect(model().attributeExists("comment"))
    }

    @Test
    fun `updateComment - failure - returns forbidden when not authenticated`() {
        val member = testMemberHelper.createActivatedMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "댓글 내용", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "인증되지 않은 수정 시도")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns not found when comment does not exist`() {
        mockMvc.perform(
            post("/comments/{commentId}", 99999L)
                .with(csrf())
                .param("content", "존재하지 않는 댓글 수정")
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns forbidden when user is not comment author`() {
        val author = testMemberHelper.createActivatedMember(email = "author@email.com", nickname = "AuthorUser")
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = author.requireId(),
                authorNickname = author.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "작성자의 댓글", author.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "다른 사용자의 수정 시도")
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns bad request when content is blank`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "원본 댓글", member.requireId())
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "")
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns bad request when content is too long`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "원본 댓글", member.requireId())
        val longContent = "a".repeat(501) // CommentContent 최대 길이 초과
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", longContent)
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns bad request when comment id is not positive`() {
        mockMvc.perform(
            post("/comments/{commentId}", -1)
                .with(csrf())
                .param("content", "댓글 수정 시도")
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - failure - returns bad request when trying to update deleted comment`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "삭제될 댓글", member.requireId())

        // 댓글 삭제
        comment.delete(member.requireId())
        commentRepository.save(comment)
        flushAndClear()

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "삭제된 댓글 수정 시도")
        )
            .andExpect(status().isBadRequest)
            .andExpect(model().attributeExists("error"))
    }

    @Test
    @WithMockMember(email = MemberFixture.DEFAULT_USERNAME)
    fun `updateComment - success - preserves comment properties after update`() {
        val member = testMemberHelper.getDefaultMember()
        val post = postRepository.save(
            PostFixture.createPost(
                authorMemberId = member.requireId(),
                authorNickname = member.nickname.value
            )
        )
        val comment = createAndSaveComment(post.requireId(), "원본 댓글", member.requireId())

        mockMvc.perform(
            post("/comments/{commentId}", comment.id)
                .with(csrf())
                .param("content", "수정된 댓글")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("comment/comments-fragment :: comment-item"))
            .andExpect(model().attributeExists("comment"))

        // 댓글이 실제로 DB에 업데이트되었는지 확인
        val updatedComment = commentRepository.findById(comment.id!!).get()
        assertEquals("수정된 댓글", updatedComment.content.text)
        assertEquals(member.requireId(), updatedComment.author.memberId)
    }

    private fun createAndSaveComment(postId: Long, text: String, authorMemberId: Long): Comment {
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
