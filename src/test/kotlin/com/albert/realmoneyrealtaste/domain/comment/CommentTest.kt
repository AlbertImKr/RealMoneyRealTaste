package com.albert.realmoneyrealtaste.domain.comment

import com.albert.realmoneyrealtaste.domain.comment.value.CommentAuthor
import com.albert.realmoneyrealtaste.domain.comment.value.CommentContent
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommentTest {

    @Test
    fun `create - success - creates comment with valid parameters`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("좋은 댓글입니다!")
        )

        assertEquals(1L, comment.postId)
        assertEquals(100L, comment.author.memberId)
        assertEquals("맛집탐험가", comment.author.nickname)
        assertEquals("좋은 댓글입니다!", comment.content.text)
        assertNull(comment.parentCommentId)
        assertEquals(CommentStatus.PUBLISHED, comment.status)
        assertNotNull(comment.createdAt)
        assertNotNull(comment.updatedAt)
    }

    @Test
    fun `create - success - creates reply comment with parent comment id`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("대댓글입니다!"),
            parentCommentId = 50L
        )

        assertEquals(1L, comment.postId)
        assertEquals(50L, comment.parentCommentId)
        assertTrue(comment.isReply())
    }

    @Test
    fun `create - failure - throws exception when postId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            Comment.create(
                postId = 0L,
                authorMemberId = 100L,
                authorNickname = "맛집탐험가",
                content = CommentContent("댓글 내용")
            )
        }.let {
            assertEquals("게시글 ID는 양수여야 합니다: 0", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when postId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            Comment.create(
                postId = -1L,
                authorMemberId = 100L,
                authorNickname = "맛집탐험가",
                content = CommentContent("댓글 내용")
            )
        }.let {
            assertEquals("게시글 ID는 양수여야 합니다: -1", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when parentCommentId is zero`() {
        assertFailsWith<IllegalArgumentException> {
            Comment.create(
                postId = 1L,
                authorMemberId = 100L,
                authorNickname = "맛집탐험가",
                content = CommentContent("대댓글 내용"),
                parentCommentId = 0L
            )
        }.let {
            assertEquals("부모 댓글 ID는 양수여야 합니다: 0", it.message)
        }
    }

    @Test
    fun `create - failure - throws exception when parentCommentId is negative`() {
        assertFailsWith<IllegalArgumentException> {
            Comment.create(
                postId = 1L,
                authorMemberId = 100L,
                authorNickname = "맛집탐험가",
                content = CommentContent("대댓글 내용"),
                parentCommentId = -1L
            )
        }.let {
            assertEquals("부모 댓글 ID는 양수여야 합니다: -1", it.message)
        }
    }

    @Test
    fun `update - success - updates comment content when author matches`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("원본 댓글")
        )
        val originalUpdatedAt = comment.updatedAt

        // 잠시 대기하여 updatedAt 변경 확인
        Thread.sleep(1)

        val newContent = CommentContent("수정된 댓글")
        comment.update(100L, newContent)

        assertEquals("수정된 댓글", comment.content.text)
        assertTrue(comment.updatedAt.isAfter(originalUpdatedAt))
    }

    @Test
    fun `update - failure - throws exception when member is not author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("원본 댓글")
        )

        assertFailsWith<IllegalArgumentException> {
            comment.update(999L, CommentContent("수정 시도"))
        }.let {
            assertEquals("댓글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `update - failure - throws exception when comment is deleted`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        // 댓글을 먼저 삭제
        comment.delete(100L)

        // 삭제된 댓글 수정 시도
        assertFailsWith<IllegalArgumentException> {
            comment.update(100L, CommentContent("수정 시도"))
        }.let {
            assertEquals("댓글이 공개 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `delete - success - deletes comment when author matches`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("삭제할 댓글")
        )
        val originalUpdatedAt = comment.updatedAt

        // 잠시 대기하여 updatedAt 변경 확인
        Thread.sleep(1)

        comment.delete(100L)

        assertEquals(CommentStatus.DELETED, comment.status)
        assertTrue(comment.isDeleted())
        assertTrue(comment.updatedAt.isAfter(originalUpdatedAt))
    }

    @Test
    fun `delete - failure - throws exception when member is not author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        assertFailsWith<IllegalArgumentException> {
            comment.delete(999L)
        }.let {
            assertEquals("댓글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `delete - failure - throws exception when comment is already deleted`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        // 댓글을 먼저 삭제
        comment.delete(100L)

        // 이미 삭제된 댓글 재삭제 시도
        assertFailsWith<IllegalArgumentException> {
            comment.delete(100L)
        }.let {
            assertEquals("댓글이 공개 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `canEditBy - success - returns true when member is author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        assertTrue(comment.canEditBy(100L))
    }

    @Test
    fun `canEditBy - success - returns false when member is not author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        assertFalse(comment.canEditBy(999L))
    }

    @Test
    fun `ensureCanEditBy - success - does not throw when member is author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        // 예외가 발생하지 않아야 함
        comment.ensureCanEditBy(100L)
    }

    @Test
    fun `ensureCanEditBy - failure - throws exception when member is not author`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        assertFailsWith<IllegalArgumentException> {
            comment.ensureCanEditBy(999L)
        }.let {
            assertEquals("댓글을 수정할 권한이 없습니다.", it.message)
        }
    }

    @Test
    fun `ensurePublished - success - does not throw when comment is published`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        // 예외가 발생하지 않아야 함
        comment.ensurePublished()
    }

    @Test
    fun `ensurePublished - failure - throws exception when comment is deleted`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        comment.delete(100L)

        assertFailsWith<IllegalArgumentException> {
            comment.ensurePublished()
        }.let {
            assertEquals("댓글이 공개 상태가 아닙니다: DELETED", it.message)
        }
    }

    @Test
    fun `isReply - success - returns true when parentCommentId is not null`() {
        val replyComment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("대댓글입니다!"),
            parentCommentId = 50L
        )

        assertTrue(replyComment.isReply())
    }

    @Test
    fun `isReply - success - returns false when parentCommentId is null`() {
        val normalComment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("일반 댓글입니다!")
        )

        assertFalse(normalComment.isReply())
    }

    @Test
    fun `isDeleted - success - returns false when comment is published`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        assertFalse(comment.isDeleted())
    }

    @Test
    fun `isDeleted - success - returns true when comment is deleted`() {
        val comment = Comment.create(
            postId = 1L,
            authorMemberId = 100L,
            authorNickname = "맛집탐험가",
            content = CommentContent("댓글 내용")
        )

        comment.delete(100L)

        assertTrue(comment.isDeleted())
    }

    @Test
    fun `setters - success - for code coverage`() {
        val testComment = TestComment()
        val expectedCreatedAt = LocalDateTime.now().minusDays(1)

        testComment.setForCoverage(
            postId = 2L,
            author = CommentAuthor(
                memberId = 200L,
                nickname = "테스트유저"
            ),
            parentCommentId = 10L,
            createdAt = expectedCreatedAt,
        )

        assertEquals(2L, testComment.postId)
        assertEquals(200L, testComment.author.memberId)
        assertEquals("테스트유저", testComment.author.nickname)
        assertEquals(10L, testComment.parentCommentId)
        assertEquals(expectedCreatedAt, testComment.createdAt)
    }

    private class TestComment : Comment(
        postId = 1L,
        author = CommentAuthor(
            memberId = 100L,
            nickname = "맛집탐험가"
        ),
        content = CommentContent("테스트 댓글"),
        parentCommentId = null,
        status = CommentStatus.PUBLISHED,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    ) {
        fun setForCoverage(
            postId: Long,
            author: CommentAuthor,
            parentCommentId: Long,
            createdAt: LocalDateTime,
        ) {
            this.postId = postId
            this.author = author
            this.parentCommentId = parentCommentId
            this.createdAt = createdAt
        }
    }
}
