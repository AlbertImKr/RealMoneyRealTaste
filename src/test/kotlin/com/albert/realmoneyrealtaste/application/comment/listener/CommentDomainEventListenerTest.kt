package com.albert.realmoneyrealtaste.application.comment.listener

import com.albert.realmoneyrealtaste.application.comment.required.CommentRepository
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class CommentDomainEventListenerTest {

    @MockK(relaxed = true)
    private lateinit var commentRepository: CommentRepository

    @InjectMockKs
    private lateinit var commentDomainEventListener: CommentDomainEventListener

    @Test
    fun `handleMemberProfileUpdated - success - updates author nickname`() {
        // Given
        val memberId = 1L
        val nickname = "새로운닉네임"
        val imageId = 999L

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = nickname,
            imageId = imageId
        )

        // When
        commentDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            commentRepository.updateAuthorNickname(
                authorMemberId = memberId,
                nickname = nickname
            )
        }
    }

    @Test
    fun `handleMemberProfileUpdated - success - does not update when nickname is null`() {
        // Given
        val memberId = 1L

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("imageId"),
            nickname = null,
            imageId = 999L
        )

        // When
        commentDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 0) {
            commentRepository.updateAuthorNickname(
                authorMemberId = any(),
                nickname = any()
            )
        }
    }

    @Test
    fun `handleCommentCreated - success - increments parent comment replies count`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val parentCommentId = 10L
        val parentCommentAuthorId = 4L
        val createdAt = LocalDateTime.now()

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = parentCommentAuthorId,
            createdAt = createdAt
        )

        // When
        commentDomainEventListener.handleCommentCreated(event)

        // Then
        verify(exactly = 1) {
            commentRepository.incrementRepliesCount(parentCommentId)
        }
    }

    @Test
    fun `handleCommentCreated - success - does not increment for top level comment`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val createdAt = LocalDateTime.now()

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = null,
            parentCommentAuthorId = null,
            createdAt = createdAt
        )

        // When
        commentDomainEventListener.handleCommentCreated(event)

        // Then
        verify(exactly = 0) {
            commentRepository.incrementRepliesCount(any())
        }
    }

    @Test
    fun `handleCommentDeleted - success - decrements parent comment replies count`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val parentCommentId = 10L
        val deletedAt = LocalDateTime.now()

        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            parentCommentId = parentCommentId,
            authorMemberId = authorMemberId,
            deletedAt = deletedAt
        )

        // When
        commentDomainEventListener.handleCommentDeleted(event)

        // Then
        verify(exactly = 1) {
            commentRepository.decrementRepliesCount(parentCommentId)
        }
    }

    @Test
    fun `handleCommentDeleted - success - does not decrement for top level comment`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val deletedAt = LocalDateTime.now()

        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            parentCommentId = null,
            authorMemberId = authorMemberId,
            deletedAt = deletedAt
        )

        // When
        commentDomainEventListener.handleCommentDeleted(event)

        // Then
        verify(exactly = 0) {
            commentRepository.decrementRepliesCount(any())
        }
    }

    @Test
    fun `integration - all event handlers work correctly`() {
        // Given
        val memberId = 1L
        val parentCommentId = 10L

        val profileEvent = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "통합테스트닉네임",
            imageId = null
        )

        val commentCreatedEvent = CommentCreatedEvent(
            commentId = 1L,
            postId = 2L,
            authorMemberId = 3L,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = 4L,
            createdAt = LocalDateTime.now()
        )

        val commentDeletedEvent = CommentDeletedEvent(
            commentId = 2L,
            postId = 2L,
            parentCommentId = parentCommentId,
            authorMemberId = 5L,
            deletedAt = LocalDateTime.now()
        )

        val nullNicknameEvent = MemberProfileUpdatedDomainEvent(
            memberId = 2L,
            email = "test2@example.com",
            updatedFields = listOf("imageId"),
            nickname = null,
            imageId = 999L
        )

        // When
        commentDomainEventListener.handleMemberProfileUpdated(profileEvent)
        commentDomainEventListener.handleCommentCreated(commentCreatedEvent)
        commentDomainEventListener.handleCommentDeleted(commentDeletedEvent)
        commentDomainEventListener.handleMemberProfileUpdated(nullNicknameEvent)

        // Then
        verify(exactly = 1) {
            commentRepository.updateAuthorNickname(
                authorMemberId = memberId,
                nickname = "통합테스트닉네임"
            )
        }

        verify(exactly = 1) {
            commentRepository.incrementRepliesCount(parentCommentId)
        }

        verify(exactly = 1) {
            commentRepository.decrementRepliesCount(parentCommentId)
        }

        // null 닉네임 이벤트는 updateAuthorNickname 호출하지 않음
        verify(exactly = 0) {
            commentRepository.updateAuthorNickname(
                authorMemberId = 2L,
                nickname = any()
            )
        }
    }

    @Test
    fun `integration - top level comments do not affect replies count`() {
        // Given
        val topLevelCommentEvent = CommentCreatedEvent(
            commentId = 3L,
            postId = 3L,
            authorMemberId = 6L,
            parentCommentId = null,
            parentCommentAuthorId = null,
            createdAt = LocalDateTime.now()
        )

        // When
        commentDomainEventListener.handleCommentCreated(topLevelCommentEvent)

        // Then
        // 최상위 댓글 이벤트는 incrementRepliesCount 호출하지 않음
        verify(exactly = 0) {
            commentRepository.incrementRepliesCount(any())
        }
    }
}
