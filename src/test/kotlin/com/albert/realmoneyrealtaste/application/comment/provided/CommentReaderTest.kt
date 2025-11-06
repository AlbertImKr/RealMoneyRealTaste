package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.application.comment.service.CommentReadService
import com.albert.realmoneyrealtaste.domain.comment.Comment
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import org.junit.jupiter.api.Assertions.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentReaderTest : IntegrationTestBase() {

    @Autowired
    private lateinit var commentReadService: CommentReadService

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Test
    fun `getComments - success - returns published comments for post`() {
        val postId = 1L
        createAndSaveComment(postId, "첫 번째 댓글")
        createAndSaveComment(postId, "두 번째 댓글")
        createAndSaveDeletedComment(postId, "삭제된 댓글")
        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending())

        val result = commentReadService.getComments(postId, pageable)

        assertAll(
            { assertEquals(2, result.content.size) },
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.any { it.content.text == "첫 번째 댓글" }) },
            { assertTrue(result.content.any { it.content.text == "두 번째 댓글" }) },
            { assertFalse(result.content.any { it.content.text == "삭제된 댓글" }) },
        )
    }

    @Test
    fun `getComments - success - returns empty page when no published comments exist`() {
        val postId = 1L
        createAndSaveDeletedComment(postId, "삭제된 댓글")
        val pageable = PageRequest.of(0, 10)

        val result = commentReadService.getComments(postId, pageable)

        assertAll(
            { assertEquals(0, result.content.size) },
            { assertEquals(0, result.totalElements) },
            { assertTrue(result.isEmpty) },
        )
    }

    @Test
    fun `getComments - success - filters comments by post ID correctly`() {
        val postId1 = 1L
        val postId2 = 2L
        createAndSaveComment(postId1, "포스트 1의 댓글")
        createAndSaveComment(postId2, "포스트 2의 댓글")
        val pageable = PageRequest.of(0, 10)

        val result1 = commentReadService.getComments(postId1, pageable)
        val result2 = commentReadService.getComments(postId2, pageable)

        assertAll(
            { assertEquals(1, result1.content.size) },
            { assertEquals(1, result2.content.size) },
            { assertEquals("포스트 1의 댓글", result1.content[0].content.text) },
            { assertEquals("포스트 2의 댓글", result2.content[0].content.text) },
        )
    }

    @Test
    fun `getComments - success - respects pagination parameters`() {
        val postId = 1L
        repeat(5) { index ->
            createAndSaveComment(postId, "댓글 ${index + 1}")
        }
        val pageable = PageRequest.of(0, 3)

        val result = commentReadService.getComments(postId, pageable)

        assertAll(
            { assertEquals(3, result.content.size) },
            { assertEquals(5, result.totalElements) },
            { assertEquals(2, result.totalPages) },
            { assertTrue(result.hasNext()) },
            { assertFalse(result.hasPrevious()) },
        )
    }

    @Test
    fun `getReplies - success - returns published replies for parent comment`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveReply(postId, parentCommentId, "첫 번째 대댓글")
        createAndSaveReply(postId, parentCommentId, "두 번째 대댓글")
        createAndSaveDeletedReply(postId, parentCommentId, "삭제된 대댓글")
        val pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending())

        val result = commentReadService.getReplies(parentCommentId, pageable)

        assertAll(
            { assertEquals(2, result.content.size) },
            { assertEquals(2, result.totalElements) },
            { assertTrue(result.content.any { it.content.text == "첫 번째 대댓글" }) },
            { assertTrue(result.content.any { it.content.text == "두 번째 대댓글" }) },
            { assertFalse(result.content.any { it.content.text == "삭제된 대댓글" }) },
        )
        result.content.forEach { reply ->
            assertEquals(parentCommentId, reply.parentCommentId)
            assertTrue(reply.isReply())
        }
    }

    @Test
    fun `getReplies - success - returns empty page when no published replies exist`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveDeletedReply(postId, parentCommentId, "삭제된 대댓글")
        val pageable = PageRequest.of(0, 10)

        val result = commentReadService.getReplies(parentCommentId, pageable)

        assertAll(
            { assertEquals(0, result.content.size) },
            { assertEquals(0, result.totalElements) },
            { assertTrue(result.isEmpty) },
        )
    }

    @Test
    fun `getReplies - success - filters replies by parent comment ID correctly`() {
        val postId = 1L
        val parentComment1 = createAndSaveComment(postId, "부모 댓글 1")
        val parentComment2 = createAndSaveComment(postId, "부모 댓글 2")
        val parentCommentId1 = parentComment1.id!!
        val parentCommentId2 = parentComment2.id!!

        createAndSaveReply(postId, parentCommentId1, "부모1의 대댓글")
        createAndSaveReply(postId, parentCommentId2, "부모2의 대댓글")
        val pageable = PageRequest.of(0, 10)

        val result1 = commentReadService.getReplies(parentCommentId1, pageable)
        val result2 = commentReadService.getReplies(parentCommentId2, pageable)

        assertAll(
            { assertEquals(1, result1.content.size) },
            { assertEquals(1, result2.content.size) },
            { assertEquals("부모1의 대댓글", result1.content[0].content.text) },
            { assertEquals("부모2의 대댓글", result2.content[0].content.text) },
            { assertEquals(parentCommentId1, result1.content[0].parentCommentId) },
            { assertEquals(parentCommentId2, result2.content[0].parentCommentId) },
        )
    }

    @Test
    fun `getReplies - success - respects pagination for replies`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        repeat(5) { index ->
            createAndSaveReply(postId, parentCommentId, "대댓글 ${index + 1}")
        }
        val pageable = PageRequest.of(0, 3)

        val result = commentReadService.getReplies(parentCommentId, pageable)

        assertAll(
            { assertEquals(3, result.content.size) },
            { assertEquals(5, result.totalElements) },
            { assertEquals(2, result.totalPages) },
            { assertTrue(result.hasNext()) },
            { assertFalse(result.hasPrevious()) },
        )
    }

    @Test
    fun `getCommentCount - success - returns count of published comments for post`() {
        val postId = 1L
        createAndSaveComment(postId, "첫 번째 댓글")
        createAndSaveComment(postId, "두 번째 댓글")
        createAndSaveDeletedComment(postId, "삭제된 댓글")

        val count = commentReadService.getCommentCount(postId)

        assertEquals(2L, count)
    }

    @Test
    fun `getCommentCount - success - returns zero when no published comments exist`() {
        val postId = 1L
        createAndSaveDeletedComment(postId, "삭제된 댓글")
        flushAndClear()

        val count = commentReadService.getCommentCount(postId)

        assertEquals(0L, count)
    }

    @Test
    fun `getCommentCount - success - filters count by post ID correctly`() {
        val postId1 = 1L
        val postId2 = 2L
        createAndSaveComment(postId1, "포스트 1의 댓글")
        createAndSaveComment(postId1, "포스트 1의 또 다른 댓글")
        createAndSaveComment(postId2, "포스트 2의 댓글")

        val count1 = commentReadService.getCommentCount(postId1)
        val count2 = commentReadService.getCommentCount(postId2)

        assertEquals(2L, count1)
        assertEquals(1L, count2)
    }

    @Test
    fun `getCommentCount - success - includes replies in count`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveReply(postId, parentCommentId, "첫 번째 대댓글")
        createAndSaveReply(postId, parentCommentId, "두 번째 대댓글")

        val count = commentReadService.getCommentCount(postId)

        assertEquals(3L, count) // 부모 댓글 1개 + 대댓글 2개
    }

    @Test
    fun `getCommentCount - success - excludes deleted replies from count`() {
        val postId = 1L
        val parentComment = createAndSaveComment(postId, "부모 댓글")
        val parentCommentId = parentComment.id!!

        createAndSaveReply(postId, parentCommentId, "공개된 대댓글")
        createAndSaveDeletedReply(postId, parentCommentId, "삭제된 대댓글")

        val count = commentReadService.getCommentCount(postId)

        assertEquals(2L, count) // 부모 댓글 1개 + 공개된 대댓글 1개
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

    private fun createAndSaveDeletedComment(postId: Long, text: String, authorMemberId: Long = 300L): Comment {
        val comment = createAndSaveComment(postId, text, authorMemberId)
        comment.delete(authorMemberId)
        return commentRepository.save(comment)
    }

    private fun createAndSaveDeletedReply(
        postId: Long,
        parentCommentId: Long,
        text: String,
        authorMemberId: Long = 400L,
    ): Comment {
        val reply = createAndSaveReply(postId, parentCommentId, text, authorMemberId)
        reply.delete(authorMemberId)
        return commentRepository.save(reply)
    }
}
