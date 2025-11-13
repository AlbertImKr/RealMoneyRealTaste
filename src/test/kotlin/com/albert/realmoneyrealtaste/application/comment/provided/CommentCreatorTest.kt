package com.albert.realmoneyrealtaste.application.comment.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.comment.dto.CommentCreateRequest
import com.albert.realmoneyrealtaste.application.comment.dto.ReplyCreateRequest
import com.albert.realmoneyrealtaste.application.comment.exception.CommentCreationException
import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.util.TestMemberHelper
import com.albert.realmoneyrealtaste.util.TestPostHelper
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RecordApplicationEvents
class CommentCreatorTest(
    private val commentRepository: CommentRepository,
    private val testMemberHelper: TestMemberHelper,
    private val testPostHelper: TestPostHelper,
    private val commentCreator: CommentCreator,
) : IntegrationTestBase() {

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @Test
    fun `createComment - success - creates a new comment`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = member.requireId())
        val expectedCommentContent = "This is a test comment."
        applicationEvents.clear()

        val comment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = expectedCommentContent
            )
        )

        val retrievedComment = commentRepository.findById(comment.requireId()).orElse(null)

        Assertions.assertAll(
            { assertNotNull(retrievedComment) },
            { assertEquals(expectedCommentContent, retrievedComment!!.content.text) },
            { assertEquals(member.requireId(), retrievedComment.author.memberId) },
            { assertEquals(member.nickname.value, retrievedComment.author.nickname) },
            { assertEquals(post.requireId(), retrievedComment.postId) },
            { assertEquals(null, retrievedComment.parentCommentId) },
            {
                assertEquals(
                    1, applicationEvents.stream(CommentCreatedEvent::class.java).count()
                )
            },
        )
    }

    @Test
    fun `createComment - failure - throws exception when member does not exist`() {
        val post = testPostHelper.createPost(authorMemberId = 1L)
        val invalidMemberId = 9999L
        val expectedCommentContent = "This is a test comment."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createComment(
                request = CommentCreateRequest(
                    memberId = invalidMemberId,
                    postId = post.requireId(),
                    content = expectedCommentContent
                )
            )
        }

        assertEquals("댓글 생성 중 오류가 발생했습니다.", exception.message)
    }

    @Test
    fun `createComment - failure - throws exception when post does not exist`() {
        val member = testMemberHelper.createActivatedMember()
        val invalidPostId = 9999L
        val expectedCommentContent = "This is a test comment."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createComment(
                request = CommentCreateRequest(
                    memberId = member.requireId(),
                    postId = invalidPostId,
                    content = expectedCommentContent
                )
            )
        }

        assertEquals("댓글 생성 중 오류가 발생했습니다.", exception.message)
    }

    @Test
    fun `createReply - success - creates a new reply to a comment`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = member.requireId())
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = "This is a parent comment."
            )
        )
        val expectedReplyContent = "This is a test reply."
        applicationEvents.clear()

        val reply = commentCreator.createReply(
            request = ReplyCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = expectedReplyContent,
                parentCommentId = parentComment.requireId()
            )
        )

        val retrievedReply = commentRepository.findById(reply.requireId()).orElse(null)

        Assertions.assertAll(
            { assertNotNull(retrievedReply) },
            { assertEquals(expectedReplyContent, retrievedReply!!.content.text) },
            { assertEquals(member.requireId(), retrievedReply.author.memberId) },
            { assertEquals(member.nickname.value, retrievedReply.author.nickname) },
            { assertEquals(post.requireId(), retrievedReply.postId) },
            { assertEquals(parentComment.requireId(), retrievedReply.parentCommentId) },
            {
                assertEquals(
                    1, applicationEvents.stream(CommentCreatedEvent::class.java).count()
                )
            },
        )
    }

    @Test
    fun `createReply - failure - throws exception when parent comment does not exist`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = member.requireId())
        val invalidParentCommentId = 9999L
        val expectedReplyContent = "This is a test reply."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = member.requireId(),
                    postId = post.requireId(),
                    content = expectedReplyContent,
                    parentCommentId = invalidParentCommentId
                )
            )
        }

        assertEquals("댓글 생성 중 오류가 발생했습니다.", exception.message)
    }

    @Test
    fun `createReply - failure - throws exception when post does not exist`() {
        val member = testMemberHelper.createActivatedMember()
        val invalidPostId = 9999L
        val expectedReplyContent = "This is a test reply."
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = testPostHelper.createPost(authorMemberId = member.requireId()).requireId(),
                content = "This is a parent comment."
            )
        )

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = member.requireId(),
                    postId = invalidPostId,
                    content = expectedReplyContent,
                    parentCommentId = parentComment.requireId()
                )
            )
        }

        assertEquals("댓글 생성 중 오류가 발생했습니다.", exception.message)
    }

    @Test
    fun `createReply - failure - throws exception when member does not exist`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = 1L)
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = "This is a parent comment."
            )
        )
        val invalidMemberId = 9999L
        val expectedReplyContent = "This is a test reply."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = invalidMemberId,
                    postId = post.requireId(),
                    content = expectedReplyContent,
                    parentCommentId = parentComment.requireId()
                )
            )
        }

        assertEquals("댓글 생성 중 오류가 발생했습니다.", exception.message)
    }

    @Test
    fun `createReply - failure - throws exception when parent comment does not belong to the post`() {
        val member = testMemberHelper.createActivatedMember()
        val post1 = testPostHelper.createPost(authorMemberId = member.requireId())
        val post2 = testPostHelper.createPost(authorMemberId = member.requireId())
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post1.requireId(),
                content = "This is a parent comment."
            )
        )
        val expectedReplyContent = "This is a test reply."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = member.requireId(),
                    postId = post2.requireId(),
                    content = expectedReplyContent,
                    parentCommentId = parentComment.requireId()
                )
            )
        }

        assertEquals(
            "댓글 생성 중 오류가 발생했습니다.",
            exception.message
        )
    }

    @Test
    fun `createReply - failure - throws exception when parent comment deleted`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = member.requireId())
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = "This is a parent comment."
            )
        )
        // 부모 댓글 삭제 처리
        parentComment.delete(member.requireId())
        commentRepository.save(parentComment)

        val expectedReplyContent = "This is a test reply."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = member.requireId(),
                    postId = post.requireId(),
                    content = expectedReplyContent,
                    parentCommentId = parentComment.requireId()
                )
            )
        }

        assertEquals(
            "댓글 생성 중 오류가 발생했습니다.",
            exception.message
        )
    }

    @Test
    fun `createReply - failure - throws exception when trying to reply to a reply`() {
        val member = testMemberHelper.createActivatedMember()
        val post = testPostHelper.createPost(authorMemberId = member.requireId())
        val parentComment = commentCreator.createComment(
            request = CommentCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = "This is a parent comment."
            )
        )
        val replyComment = commentCreator.createReply(
            request = ReplyCreateRequest(
                memberId = member.requireId(),
                postId = post.requireId(),
                content = "This is a reply to the parent comment.",
                parentCommentId = parentComment.requireId()
            )
        )

        val expectedReplyContent = "This is a reply to a reply."

        val exception = assertFailsWith<CommentCreationException> {
            commentCreator.createReply(
                request = ReplyCreateRequest(
                    memberId = member.requireId(),
                    postId = post.requireId(),
                    content = expectedReplyContent,
                    parentCommentId = replyComment.requireId()
                )
            )
        }

        assertEquals(
            "댓글 생성 중 오류가 발생했습니다.",
            exception.message
        )
    }
}
