package com.albert.realmoneyrealtaste.application.post.listener

import com.albert.realmoneyrealtaste.application.post.required.PostRepository
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
class PostDomainEventListenerTest {

    @MockK(relaxed = true)
    private lateinit var postRepository: PostRepository

    @InjectMockKs
    private lateinit var postDomainEventListener: PostDomainEventListener

    @Test
    fun `handleMemberProfileUpdated - success - updates author info with nickname and imageId`() {
        // Given
        val memberId = 1L
        val email = "test@example.com"
        val nickname = "새로운닉네임"
        val imageId = 999L
        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = email,
            updatedFields = listOf("nickname", "imageId"),
            nickname = nickname,
            imageId = imageId
        )

        // When
        postDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            postRepository.updateAuthorInfo(
                authorMemberId = memberId,
                nickname = nickname,
                introduction = null,
                imageId = imageId
            )
        }
    }

    @Test
    fun `handleMemberProfileUpdated - success - updates author info with only nickname`() {
        // Given
        val memberId = 1L
        val email = "test@example.com"
        val nickname = "바뀐닉네임"
        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = email,
            updatedFields = listOf("nickname"),
            nickname = nickname,
            imageId = null
        )

        // When
        postDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            postRepository.updateAuthorInfo(
                authorMemberId = memberId,
                nickname = nickname,
                introduction = null,
                imageId = null
            )
        }
    }

    @Test
    fun `handleMemberProfileUpdated - success - updates author info with only imageId`() {
        // Given
        val memberId = 1L
        val email = "test@example.com"
        val imageId = 888L
        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = email,
            updatedFields = listOf("imageId"),
            nickname = null,
            imageId = imageId
        )

        // When
        postDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            postRepository.updateAuthorInfo(
                authorMemberId = memberId,
                nickname = null,
                introduction = null,
                imageId = imageId
            )
        }
    }

    @Test
    fun `handleCommentCreated - success - increments post comment count`() {
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
        postDomainEventListener.handleCommentCreated(event)

        // Then
        verify(exactly = 1) {
            postRepository.incrementCommentCount(postId)
        }
    }

    @Test
    fun `handleCommentCreated - success - increments comment count for reply`() {
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
        postDomainEventListener.handleCommentCreated(event)

        // Then
        verify(exactly = 1) {
            postRepository.incrementCommentCount(postId)
        }
    }

    @Test
    fun `handleCommentDeleted - success - decrements post comment count`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val deletedAt = LocalDateTime.now()
        val authorMemberId = 3L
        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            deletedAt = deletedAt,
            parentCommentId = null,
            authorMemberId = authorMemberId,
        )

        // When
        postDomainEventListener.handleCommentDeleted(event)

        // Then
        verify(exactly = 1) {
            postRepository.decrementCommentCount(postId)
        }
    }

    @Test
    fun `handleCommentDeleted - success - decrements comment count for deleted reply`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val parentCommentId = 10L
        val deletedAt = LocalDateTime.now()
        val authorMemberId = 3L
        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            parentCommentId = parentCommentId,
            deletedAt = deletedAt,
            authorMemberId = authorMemberId,
        )

        // When
        postDomainEventListener.handleCommentDeleted(event)

        // Then
        verify(exactly = 1) {
            postRepository.decrementCommentCount(postId)
        }
    }

    @Test
    fun `integration - all event handlers work correctly`() {
        // Given
        val memberId = 1L
        val postId = 2L
        val authorMemberId = 3L

        val profileEvent = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "통합테스트닉네임",
            imageId = null
        )

        val commentCreatedEvent = CommentCreatedEvent(
            commentId = 1L,
            postId = postId,
            authorMemberId = 3L,
            parentCommentId = null,
            parentCommentAuthorId = null,
            createdAt = LocalDateTime.now()
        )

        val commentDeletedEvent = CommentDeletedEvent(
            commentId = 2L,
            postId = postId,
            parentCommentId = null,
            deletedAt = LocalDateTime.now(),
            authorMemberId = authorMemberId,
        )

        // When
        postDomainEventListener.handleMemberProfileUpdated(profileEvent)
        postDomainEventListener.handleCommentCreated(commentCreatedEvent)
        postDomainEventListener.handleCommentDeleted(commentDeletedEvent)

        // Then
        verify(exactly = 1) {
            postRepository.updateAuthorInfo(
                authorMemberId = memberId,
                nickname = "통합테스트닉네임",
                introduction = null,
                imageId = null
            )
        }

        verify(exactly = 1) {
            postRepository.incrementCommentCount(postId)
        }

        verify(exactly = 1) {
            postRepository.decrementCommentCount(postId)
        }
    }
}
