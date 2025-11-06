package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.dto.CommentUpdateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentNotFoundException
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.exceptions.UnauthorizedCommentOperationException
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CommentUpdaterTest : IntegrationTestBase() {

    @Autowired
    private lateinit var commentUpdater: CommentUpdater

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var testMemberHelper: TestMemberHelper

    @Test
    fun `updateComment - success - updates comment content`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "원본 댓글 내용"
        )
        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "수정된 댓글 내용",
            memberId = author.requireId()
        )

        // when
        val updatedComment = commentUpdater.updateComment(request)

        // then
        assertEquals("수정된 댓글 내용", updatedComment.content.text)
        assertEquals(comment.id, updatedComment.id)

        // DB에서도 확인
        val savedComment = commentRepository.findById(comment.requireId()).get()
        assertEquals("수정된 댓글 내용", savedComment.content.text)
    }

    @Test
    fun `updateComment - success - author can update their own comment`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "작성자의 댓글"
        )
        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "작성자가 수정한 댓글",
            memberId = author.requireId()
        )

        // when
        val updatedComment = commentUpdater.updateComment(request)

        // then
        assertEquals("작성자가 수정한 댓글", updatedComment.content.text)
        assertEquals(author.requireId(), updatedComment.author.memberId)
    }

    @Test
    fun `updateComment - failure - throws exception when comment not found`() {
        // given
        val member = testMemberHelper.createActivatedMember()
        val request = CommentUpdateRequest(
            commentId = 99999L,
            content = "존재하지 않는 댓글 수정",
            memberId = member.requireId()
        )

        // when & then
        val exception = assertFailsWith<CommentNotFoundException> {
            commentUpdater.updateComment(request)
        }
        assertTrue(exception.message!!.contains("댓글을 찾을 수 없습니다"))
    }

    @Test
    fun `updateComment - failure - throws exception when member not authorized`() {
        // given
        val author = testMemberHelper.createActivatedMember(email = "author@test.com")
        val otherMember = testMemberHelper.createActivatedMember(email = "other@test.com")
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "작성자의 댓글"
        )
        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "다른 사용자가 수정 시도",
            memberId = otherMember.requireId()
        )

        // when & then
        assertFailsWith<UnauthorizedCommentOperationException> {
            commentUpdater.updateComment(request)
        }
    }

    @Test
    fun `updateComment - failure - throws exception when comment is deleted`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "삭제될 댓글"
        )

        // 댓글 삭제
        comment.delete(author.requireId())
        commentRepository.save(comment)
        flushAndClear()

        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "삭제된 댓글 수정 시도",
            memberId = author.requireId()
        )

        // when & then
        val exception = assertFailsWith<CommentNotFoundException> {
            commentUpdater.updateComment(request)
        }
        assertTrue(exception.message!!.contains("삭제된 댓글은 수정할 수 없습니다"))
    }

    @Test
    fun `updateComment - failure - throws exception when member not found`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "댓글 내용"
        )
        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "수정된 내용",
            memberId = 99999L // 존재하지 않는 회원 ID
        )

        // when & then
        assertFailsWith<Exception> { // MemberNotFoundException 또는 관련 예외
            commentUpdater.updateComment(request)
        }
    }

    @Test
    fun `updateComment - success - updates reply comment`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val parentComment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "부모 댓글"
        )
        val replyComment = createAndSaveReply(
            postId = 1L,
            parentCommentId = parentComment.requireId(),
            authorMemberId = author.requireId(),
            content = "원본 대댓글"
        )

        val request = CommentUpdateRequest(
            commentId = replyComment.requireId(),
            content = "수정된 대댓글",
            memberId = author.requireId()
        )

        // when
        val updatedComment = commentUpdater.updateComment(request)

        // then
        assertEquals("수정된 대댓글", updatedComment.content.text)
        assertTrue(updatedComment.isReply())
    }

    @Test
    fun `updateComment - success - preserves other comment properties`() {
        // given
        val author = testMemberHelper.createActivatedMember()
        val comment = createAndSaveComment(
            postId = 1L,
            authorMemberId = author.requireId(),
            content = "원본 댓글"
        )
        val originalCreatedAt = comment.createdAt
        val originalPostId = comment.postId

        val request = CommentUpdateRequest(
            commentId = comment.requireId(),
            content = "수정된 댓글",
            memberId = author.requireId()
        )

        // when
        val updatedComment = commentUpdater.updateComment(request)

        // then
        assertEquals("수정된 댓글", updatedComment.content.text)
        assertEquals(originalCreatedAt, updatedComment.createdAt)
        assertEquals(originalPostId, updatedComment.postId)
        assertEquals(author.requireId(), updatedComment.author.memberId)
    }

    // Helper methods

    private fun createAndSaveComment(
        postId: Long,
        authorMemberId: Long,
        content: String,
    ): Comment {
        val comment = Comment.create(
            postId = postId,
            authorMemberId = authorMemberId,
            authorNickname = "테스트유저$authorMemberId",
            content = CommentContent(content)
        )
        return commentRepository.save(comment)
    }

    private fun createAndSaveReply(
        postId: Long,
        parentCommentId: Long,
        authorMemberId: Long,
        content: String,
    ): Comment {
        val reply = Comment.create(
            postId = postId,
            authorMemberId = authorMemberId,
            authorNickname = "테스트유저$authorMemberId",
            content = CommentContent(content),
            parentCommentId = parentCommentId
        )
        return commentRepository.save(reply)
    }
}

