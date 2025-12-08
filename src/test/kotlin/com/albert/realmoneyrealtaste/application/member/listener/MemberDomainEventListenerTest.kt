package com.albert.realmoneyrealtaste.application.member.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.application.member.event.EmailSendRequestedEvent
import com.albert.realmoneyrealtaste.application.member.provided.ActivationTokenGenerator
import com.albert.realmoneyrealtaste.application.member.required.MemberRepository
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.event.FriendRequestAcceptedEvent
import com.albert.realmoneyrealtaste.domain.friend.event.FriendshipTerminatedEvent
import com.albert.realmoneyrealtaste.domain.member.ActivationToken
import com.albert.realmoneyrealtaste.domain.member.event.MemberRegisteredDomainEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostCreatedEvent
import com.albert.realmoneyrealtaste.domain.post.event.PostDeletedEvent
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class MemberDomainEventListenerTest {

    @MockK(relaxed = true)
    private lateinit var activationTokenGenerator: ActivationTokenGenerator

    @MockK(relaxed = true)
    private lateinit var eventPublisher: ApplicationEventPublisher

    @MockK(relaxed = true)
    private lateinit var memberRepository: MemberRepository

    @MockK(relaxed = true)
    private lateinit var friendshipRepository: FriendshipRepository

    @InjectMockKs
    private lateinit var memberDomainEventListener: MemberDomainEventListener

    @Test
    fun `handlePostCreated - success - increments member post count`() {
        // Given
        val authorMemberId = 1L
        val postId = 2L
        val restaurantName = "맛있는 식당"
        val occurredAt = LocalDateTime.now()

        val event = PostCreatedEvent(
            postId = postId,
            authorMemberId = authorMemberId,
            restaurantName = restaurantName,
            occurredAt = occurredAt,
        )

        // When
        memberDomainEventListener.handlePostCreated(event)

        // Then
        verify(exactly = 1) {
            memberRepository.incrementPostCount(authorMemberId)
        }
    }

    @Test
    fun `handlePostDeleted - success - decrements member post count`() {
        // Given
        val authorMemberId = 1L
        val postId = 2L
        val occurredAt = LocalDateTime.now()

        val event = PostDeletedEvent(
            postId = postId,
            authorMemberId = authorMemberId,
            occurredAt = occurredAt
        )

        // When
        memberDomainEventListener.handlePostDeleted(event)

        // Then
        verify(exactly = 1) {
            memberRepository.decrementPostCount(authorMemberId)
        }
    }

    @Test
    fun `handleFriendRequestAccepted - success - updates follower and following counts`() {
        // Given
        val friendshipId = 4L
        val fromMemberId = 1L
        val toMemberId = 2L
        val fromMemberFollowingsCount = 5L
        val toMemberFollowersCount = 3L

        val event = FriendRequestAcceptedEvent(
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            friendshipId = friendshipId,
            occurredAt = LocalDateTime.now()
        )

        every {
            friendshipRepository.countFriends(fromMemberId, FriendshipStatus.ACCEPTED)
        } returns fromMemberFollowingsCount

        every {
            friendshipRepository.countFriends(toMemberId, FriendshipStatus.ACCEPTED)
        } returns toMemberFollowersCount

        // When
        memberDomainEventListener.handleFriendRequestAccepted(event)

        // Then
        verifyOrder {
            friendshipRepository.countFriends(fromMemberId, FriendshipStatus.ACCEPTED)
            memberRepository.updateFollowingsCount(fromMemberId, fromMemberFollowingsCount)

            friendshipRepository.countFriends(toMemberId, FriendshipStatus.ACCEPTED)
            memberRepository.updateFollowersCount(toMemberId, toMemberFollowersCount)
        }
    }

    @Test
    fun `handleFriendRequestAccepted - success - handles zero counts`() {
        // Given
        val fromMemberId = 1L
        val toMemberId = 2L
        val friendshipId = 3L

        val event = FriendRequestAcceptedEvent(
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            friendshipId = friendshipId,
            occurredAt = LocalDateTime.now(),
        )

        every {
            friendshipRepository.countFriends(fromMemberId, FriendshipStatus.ACCEPTED)
        } returns 0

        every {
            friendshipRepository.countFriends(toMemberId, FriendshipStatus.ACCEPTED)
        } returns 0

        // When
        memberDomainEventListener.handleFriendRequestAccepted(event)

        // Then
        verify(exactly = 1) {
            memberRepository.updateFollowingsCount(fromMemberId, 0)
        }
        verify(exactly = 1) {
            memberRepository.updateFollowersCount(toMemberId, 0)
        }
    }

    @Test
    fun `handleFriendshipTerminated - success - updates follower and following counts`() {
        // Given
        val memberId = 1L
        val friendMemberId = 2L
        val memberFollowingsCount = 4L
        val friendFollowersCount = 2L
        val friendshipId = 3L

        val event = FriendshipTerminatedEvent(
            friendshipId = friendshipId,
            memberId = memberId,
            friendMemberId = friendMemberId,
            occurredAt = LocalDateTime.now()
        )

        every {
            friendshipRepository.countFriends(memberId, FriendshipStatus.ACCEPTED)
        } returns memberFollowingsCount

        every {
            friendshipRepository.countFriends(friendMemberId, FriendshipStatus.ACCEPTED)
        } returns friendFollowersCount

        // When
        memberDomainEventListener.handleFriendshipTerminated(event)

        // Then
        verifyOrder {
            friendshipRepository.countFriends(memberId, FriendshipStatus.ACCEPTED)
            memberRepository.updateFollowingsCount(memberId, memberFollowingsCount)

            friendshipRepository.countFriends(friendMemberId, FriendshipStatus.ACCEPTED)
            memberRepository.updateFollowersCount(friendMemberId, friendFollowersCount)
        }
    }

    @Test
    fun `handleFriendshipTerminated - success - handles single friend remaining`() {
        // Given
        val memberId = 1L
        val friendMemberId = 2L
        val friendshipId = 3L

        val event = FriendshipTerminatedEvent(
            memberId = memberId,
            friendMemberId = friendMemberId,
            occurredAt = LocalDateTime.now(),
            friendshipId = friendshipId
        )

        every {
            friendshipRepository.countFriends(memberId, FriendshipStatus.ACCEPTED)
        } returns 1

        every {
            friendshipRepository.countFriends(friendMemberId, FriendshipStatus.ACCEPTED)
        } returns 1

        // When
        memberDomainEventListener.handleFriendshipTerminated(event)

        // Then
        verify(exactly = 1) {
            memberRepository.updateFollowingsCount(memberId, 1)
        }
        verify(exactly = 1) {
            memberRepository.updateFollowersCount(friendMemberId, 1)
        }
    }

    @Test
    fun `handleMemberRegistered - success - generates token and publishes activation email event`() {
        // Given
        val memberId = 1L
        val email = "test@example.com"
        val nickname = "테스트유저"
        val activationToken = ActivationToken(
            memberId = memberId,
            token = "test-activation-token-123",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusHours(24)
        )

        val event = MemberRegisteredDomainEvent(
            memberId = memberId,
            email = email,
            nickname = nickname,
            occurredAt = LocalDateTime.now(),
        )

        every { activationTokenGenerator.generate(memberId) } returns activationToken

        val eventSlot = slot<EmailSendRequestedEvent.ActivationEmail>()

        // When
        memberDomainEventListener.handleMemberRegistered(event)

        // Then
        verify(exactly = 1) { activationTokenGenerator.generate(memberId) }
        verify(exactly = 1) {
            eventPublisher.publishEvent(capture(eventSlot))
        }

        val publishedEvent = eventSlot.captured
        assertEquals(email, publishedEvent.email.address)
        assertEquals(nickname, publishedEvent.nickname.value)
        assertEquals(activationToken, publishedEvent.activationToken)
    }

    @Test
    fun `handleMemberRegistered - success - handles different member data`() {
        // Given
        val memberId = 999L
        val email = "different@example.com"
        val nickname = "다른유저"
        val activationToken = ActivationToken(
            memberId = memberId,
            token = "different-token-456",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusHours(24)
        )

        val event = MemberRegisteredDomainEvent(
            memberId = memberId,
            email = email,
            nickname = nickname,
        )

        every { activationTokenGenerator.generate(memberId) } returns activationToken

        val eventSlot = slot<EmailSendRequestedEvent.ActivationEmail>()

        // When
        memberDomainEventListener.handleMemberRegistered(event)

        // Then
        verify(exactly = 1) { activationTokenGenerator.generate(memberId) }
        verify(exactly = 1) {
            eventPublisher.publishEvent(capture(eventSlot))
        }

        val publishedEvent = eventSlot.captured
        assertEquals(email, publishedEvent.email.address)
        assertEquals(nickname, publishedEvent.nickname.value)
        assertEquals(activationToken, publishedEvent.activationToken)
    }

    @Test
    fun `integration - all event handlers work correctly`() {
        // Given
        val memberId = 1L
        val postId = 2L
        val fromMemberId = 3L
        val toMemberId = 4L
        val friendshipId = 5L
        val activationToken = ActivationToken(
            memberId = memberId,
            token = "integration-token-123",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusHours(24)
        )

        val postCreatedEvent = PostCreatedEvent(
            postId = postId,
            authorMemberId = memberId,
            restaurantName = "통합테스트",
            occurredAt = LocalDateTime.now()
        )

        val postDeletedEvent = PostDeletedEvent(
            postId = postId,
            authorMemberId = memberId,
            occurredAt = LocalDateTime.now(),
        )

        val friendRequestEvent = FriendRequestAcceptedEvent(
            fromMemberId = fromMemberId,
            toMemberId = toMemberId,
            friendshipId = friendshipId,
            occurredAt = LocalDateTime.now(),
        )

        val memberRegisteredEvent = MemberRegisteredDomainEvent(
            memberId = memberId,
            email = "integration@example.com",
            nickname = "통합테스트",
        )

        every {
            friendshipRepository.countFriends(fromMemberId, FriendshipStatus.ACCEPTED)
        } returns 10

        every {
            friendshipRepository.countFriends(toMemberId, FriendshipStatus.ACCEPTED)
        } returns 5

        every { activationTokenGenerator.generate(memberId) } returns activationToken

        // When
        memberDomainEventListener.handlePostCreated(postCreatedEvent)
        memberDomainEventListener.handlePostDeleted(postDeletedEvent)
        memberDomainEventListener.handleFriendRequestAccepted(friendRequestEvent)
        memberDomainEventListener.handleMemberRegistered(memberRegisteredEvent)

        // Then
        verify(exactly = 1) { memberRepository.incrementPostCount(memberId) }
        verify(exactly = 1) { memberRepository.decrementPostCount(memberId) }
        verify(exactly = 1) { memberRepository.updateFollowingsCount(fromMemberId, 10) }
        verify(exactly = 1) { memberRepository.updateFollowersCount(toMemberId, 5) }
        verify(exactly = 1) { activationTokenGenerator.generate(memberId) }
        verify(exactly = 1) {
            eventPublisher.publishEvent(any<EmailSendRequestedEvent.ActivationEmail>())
        }
    }
}
