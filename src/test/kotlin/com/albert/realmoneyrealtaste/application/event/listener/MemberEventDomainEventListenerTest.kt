package com.albert.realmoneyrealtaste.application.event.listener

import com.albert.realmoneyrealtaste.application.event.MemberEventCreationService
import com.albert.realmoneyrealtaste.domain.comment.event.CommentCreatedEvent
import com.albert.realmoneyrealtaste.domain.comment.event.CommentDeletedEvent
import com.albert.realmoneyrealtaste.domain.event.MemberEventType
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestRejectedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestSentEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberActivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberDeactivatedDomainEvent
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class MemberEventDomainEventListenerTest {

    @MockK(relaxed = true)
    private lateinit var memberEventCreationService: MemberEventCreationService

    @InjectMockKs
    private lateinit var memberEventDomainEventListener: MemberEventDomainEventListener

    // ===== 친구 관련 이벤트 테스트 =====

    @Test
    fun `handleFriendRequestSent - success - creates events for both members`() {
        // Given
        val fromMemberId = 1L
        val toMemberId = 2L

        val event = FriendRequestSentEvent(
            friendshipId = 10L,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
        )

        // When
        memberEventDomainEventListener.handleFriendRequestSent(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = fromMemberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "친구 요청을 보냈습니다",
                message = "${toMemberId}님에게 친구 요청을 보냈습니다.",
                relatedMemberId = toMemberId
            )
        }

        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = toMemberId,
                eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
                title = "친구 요청을 받았습니다",
                message = "${fromMemberId}님이 친구 요청을 보냈습니다.",
                relatedMemberId = fromMemberId
            )
        }
    }

    @Test
    fun `handleFriendRequestAccepted - success - creates events for both members`() {
        // Given
        val fromMemberId = 1L
        val toMemberId = 2L

        val event = FriendRequestAcceptedEvent(
            friendshipId = 10L,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            occurredAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleFriendRequestAccepted(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = fromMemberId,
                eventType = MemberEventType.FRIEND_REQUEST_ACCEPTED,
                title = "친구 요청이 수락되었습니다",
                message = "${toMemberId}님이 친구 요청을 수락했습니다.",
                relatedMemberId = toMemberId
            )
        }

        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = toMemberId,
                eventType = MemberEventType.FRIEND_REQUEST_ACCEPTED,
                title = "친구 요청을 수락했습니다",
                message = "${fromMemberId}님의 친구 요청을 수락했습니다.",
                relatedMemberId = fromMemberId
            )
        }
    }

    @Test
    fun `handleFriendRequestRejected - success - creates event for requester`() {
        // Given
        val fromMemberId = 1L
        val toMemberId = 2L

        val event = FriendRequestRejectedEvent(
            friendshipId = 10L,
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
        )

        // When
        memberEventDomainEventListener.handleFriendRequestRejected(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = fromMemberId,
                eventType = MemberEventType.FRIEND_REQUEST_REJECTED,
                title = "친구 요청이 거절되었습니다",
                message = "${toMemberId}님이 친구 요청을 거절했습니다.",
                relatedMemberId = toMemberId
            )
        }
    }

    @Test
    fun `handleFriendshipTerminated - success - creates event for member`() {
        // Given
        val memberId = 1L
        val friendMemberId = 2L

        val event = FriendshipTerminatedEvent(
            friendshipId = 10L,
            memberId = memberId,
            friendMemberId = friendMemberId,
            occurredAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleFriendshipTerminated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIENDSHIP_TERMINATED,
                title = "친구 관계가 해제되었습니다",
                message = "${friendMemberId}님과의 친구 관계가 해제되었습니다.",
                relatedMemberId = friendMemberId
            )
        }
    }

    // ===== 게시물 관련 이벤트 테스트 =====

    @Test
    fun `handlePostCreated - success - creates event for author`() {
        // Given
        val postId = 1L
        val authorMemberId = 2L

        val event = PostCreatedEvent(
            postId = postId,
            authorMemberId = authorMemberId,
            restaurantName = "맛있는집",
            occurredAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handlePostCreated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.POST_CREATED,
                title = "새 게시물을 작성했습니다",
                message = "새로운 맛집 게시물을 작성했습니다.",
                relatedPostId = postId
            )
        }
    }

    @Test
    fun `handlePostDeleted - success - creates event for author`() {
        // Given
        val postId = 1L
        val authorMemberId = 2L

        val event = PostDeletedEvent(
            postId = postId,
            authorMemberId = authorMemberId,
            occurredAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handlePostDeleted(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.POST_DELETED,
                title = "게시물을 삭제했습니다",
                message = "게시물을 삭제했습니다.",
                relatedPostId = postId
            )
        }
    }

    // ===== 댓글 관련 이벤트 테스트 =====

    @Test
    fun `handleCommentCreated - success - creates event for top level comment`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = null,
            parentCommentAuthorId = null,
            createdAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleCommentCreated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.COMMENT_CREATED,
                title = "댓글을 작성했습니다",
                message = "댓글을 작성했습니다.",
                relatedPostId = postId,
                relatedCommentId = commentId
            )
        }

        // 부모 댓글 작성자에게는 이벤트 생성되지 않아야 함
        verify(exactly = 0) {
            memberEventCreationService.createEvent(
                memberId = any(),
                eventType = MemberEventType.COMMENT_REPLIED,
                title = any(),
                message = any(),
                relatedPostId = any(),
                relatedCommentId = any()
            )
        }
    }

    @Test
    fun `handleCommentCreated - success - creates events for reply to different author`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val parentCommentId = 10L
        val parentCommentAuthorId = 4L

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = parentCommentAuthorId,
            createdAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleCommentCreated(event)

        // Then
        // 댓글 작성자 이벤트
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.COMMENT_CREATED,
                title = "댓글을 작성했습니다",
                message = "댓글을 작성했습니다.",
                relatedPostId = postId,
                relatedCommentId = commentId
            )
        }

        // 부모 댓글 작성자에게 알림 이벤트
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = parentCommentAuthorId,
                eventType = MemberEventType.COMMENT_REPLIED,
                title = "대댓글이 달렸습니다",
                message = "댓글에 대댓글이 달렸습니다.",
                relatedPostId = postId,
                relatedCommentId = parentCommentId
            )
        }
    }

    @Test
    fun `handleCommentCreated - success - creates single event for reply to self`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L
        val parentCommentId = 10L

        val event = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = authorMemberId,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = authorMemberId, // 자신의 댓글에 답글
            createdAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleCommentCreated(event)

        // Then
        // 댓글 작성자 이벤트만 생성
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.COMMENT_CREATED,
                title = "댓글을 작성했습니다",
                message = "댓글을 작성했습니다.",
                relatedPostId = postId,
                relatedCommentId = commentId
            )
        }

        // 자신에게는 알림 이벤트 생성되지 않아야 함
        verify(exactly = 0) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.COMMENT_REPLIED,
                title = any(),
                message = any(),
                relatedPostId = any(),
                relatedCommentId = any()
            )
        }
    }

    @Test
    fun `handleCommentDeleted - success - creates event for author`() {
        // Given
        val commentId = 1L
        val postId = 2L
        val authorMemberId = 3L

        val event = CommentDeletedEvent(
            commentId = commentId,
            postId = postId,
            parentCommentId = null,
            authorMemberId = authorMemberId,
            deletedAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleCommentDeleted(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = authorMemberId,
                eventType = MemberEventType.COMMENT_DELETED,
                title = "댓글을 삭제했습니다",
                message = "댓글을 삭제했습니다.",
                relatedPostId = postId,
                relatedCommentId = commentId
            )
        }
    }

    // ===== 회원 관련 이벤트 테스트 =====

    @Test
    fun `handleMemberActivated - success - creates activation event`() {
        // Given
        val memberId = 1L
        val email = "example@email.com"

        val event = MemberActivatedDomainEvent(
            memberId = memberId,
            email = email,
            nickname = "example",
        )

        // When
        memberEventDomainEventListener.handleMemberActivated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.ACCOUNT_ACTIVATED,
                title = "계정이 활성화되었습니다",
                message = "회원님의 계정이 성공적으로 활성화되었습니다."
            )
        }
    }

    @Test
    fun `handleMemberDeactivated - success - creates deactivation event`() {
        // Given
        val memberId = 1L

        val event = MemberDeactivatedDomainEvent(
            memberId = memberId,
        )

        // When
        memberEventDomainEventListener.handleMemberDeactivated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.ACCOUNT_DEACTIVATED,
                title = "계정이 비활성화되었습니다",
                message = "회원님의 계정이 비활성화되었습니다."
            )
        }
    }

    @Test
    fun `handleMemberProfileUpdated - success - creates profile update event`() {
        // Given
        val memberId = 1L

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "새닉네임",
            imageId = null
        )

        // When
        memberEventDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.PROFILE_UPDATED,
                title = "프로필이 업데이트되었습니다",
                message = "회원님의 프로필 정보가 업데이트되었습니다."
            )
        }
    }

    // ===== 통합 테스트 =====

    @Test
    fun `integration - multiple event handlers work correctly`() {
        // Given
        val memberId = 1L
        val friendId = 2L
        val postId = 3L
        val commentId = 4L
        val parentCommentId = 5L
        val email = "example@email.com"
        val nickname = "example"

        val friendRequestEvent = FriendRequestSentEvent(
            friendshipId = 10L,
            fromMemberId = memberId,
            toMemberId = friendId,
            occurredAt = LocalDateTime.now()
        )

        val postCreatedEvent = PostCreatedEvent(
            postId = postId,
            authorMemberId = memberId,
            restaurantName = "통합테스트",
            occurredAt = LocalDateTime.now()
        )

        val commentCreatedEvent = CommentCreatedEvent(
            commentId = commentId,
            postId = postId,
            authorMemberId = memberId,
            parentCommentId = parentCommentId,
            parentCommentAuthorId = friendId,
            createdAt = LocalDateTime.now()
        )

        val memberActivatedEvent = MemberActivatedDomainEvent(
            memberId = memberId,
            email = email,
            nickname = nickname,
            occurredAt = LocalDateTime.now()
        )

        // When
        memberEventDomainEventListener.handleFriendRequestSent(friendRequestEvent)
        memberEventDomainEventListener.handlePostCreated(postCreatedEvent)
        memberEventDomainEventListener.handleCommentCreated(commentCreatedEvent)
        memberEventDomainEventListener.handleMemberActivated(memberActivatedEvent)

        // Then
        // 친구 요청 이벤트 2개
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.FRIEND_REQUEST_SENT,
                title = "친구 요청을 보냈습니다",
                message = "${friendId}님에게 친구 요청을 보냈습니다.",
                relatedMemberId = friendId
            )
        }

        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = friendId,
                eventType = MemberEventType.FRIEND_REQUEST_RECEIVED,
                title = "친구 요청을 받았습니다",
                message = "${memberId}님이 친구 요청을 보냈습니다.",
                relatedMemberId = memberId
            )
        }

        // 게시물 생성 이벤트 1개
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.POST_CREATED,
                title = "새 게시물을 작성했습니다",
                message = "새로운 맛집 게시물을 작성했습니다.",
                relatedPostId = postId
            )
        }

        // 댓글 관련 이벤트 2개 (작성자 + 부모 댓글 작성자)
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.COMMENT_CREATED,
                title = "댓글을 작성했습니다",
                message = "댓글을 작성했습니다.",
                relatedPostId = postId,
                relatedCommentId = commentId
            )
        }

        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = friendId,
                eventType = MemberEventType.COMMENT_REPLIED,
                title = "대댓글이 달렸습니다",
                message = "댓글에 대댓글이 달렸습니다.",
                relatedPostId = postId,
                relatedCommentId = parentCommentId
            )
        }

        // 회원 활성화 이벤트 1개
        verify(exactly = 1) {
            memberEventCreationService.createEvent(
                memberId = memberId,
                eventType = MemberEventType.ACCOUNT_ACTIVATED,
                title = "계정이 활성화되었습니다",
                message = "회원님의 계정이 성공적으로 활성화되었습니다."
            )
        }
    }
}
