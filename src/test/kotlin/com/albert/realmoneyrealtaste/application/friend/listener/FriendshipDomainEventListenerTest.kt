package com.albert.realmoneyrealtaste.application.friend.listener

import com.albert.realmoneyrealtaste.application.friend.required.FriendshipRepository
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.member.event.MemberProfileUpdatedDomainEvent
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FriendshipDomainEventListenerTest {

    @MockK(relaxed = true)
    private lateinit var friendshipRepository: FriendshipRepository

    @InjectMockKs
    private lateinit var friendshipDomainEventListener: FriendshipDomainEventListener

    @Test
    fun `handleMemberProfileUpdated - success - updates single friendship`() {
        // Given
        val memberId = 1L
        val nickname = "새로운닉네임"
        val imageId = 999L

        val friendship = mockk<Friendship>(relaxed = true)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname", "imageId"),
            nickname = nickname,
            imageId = imageId
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns listOf(friendship)

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) { friendshipRepository.findAllActiveByMemberId(memberId) }
        verify(exactly = 1) {
            friendship.updateMemberInfo(
                memberId = memberId,
                nickname = nickname,
                imageId = imageId
            )
        }
        verify(exactly = 1) { friendshipRepository.save(friendship) }

        verifyOrder {
            friendship.updateMemberInfo(memberId, nickname, imageId)
            friendshipRepository.save(friendship)
        }
    }

    @Test
    fun `handleMemberProfileUpdated - success - updates multiple friendships`() {
        // Given
        val memberId = 1L
        val nickname = "업데이트닉네임"
        val imageId = 888L

        val friendship1 = mockk<Friendship>(relaxed = true)
        val friendship2 = mockk<Friendship>(relaxed = true)
        val friendship3 = mockk<Friendship>(relaxed = true)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = nickname,
            imageId = imageId
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns listOf(
            friendship1, friendship2, friendship3
        )

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) { friendshipRepository.findAllActiveByMemberId(memberId) }

        // 각 친구 관계에 대해 updateMemberInfo 호출 확인
        verify(exactly = 1) {
            friendship1.updateMemberInfo(
                memberId = memberId,
                nickname = nickname,
                imageId = imageId
            )
        }
        verify(exactly = 1) {
            friendship2.updateMemberInfo(
                memberId = memberId,
                nickname = nickname,
                imageId = imageId
            )
        }
        verify(exactly = 1) {
            friendship3.updateMemberInfo(
                memberId = memberId,
                nickname = nickname,
                imageId = imageId
            )
        }

        // 각 친구 관계 저장 확인
        verify(exactly = 1) { friendshipRepository.save(friendship1) }
        verify(exactly = 1) { friendshipRepository.save(friendship2) }
        verify(exactly = 1) { friendshipRepository.save(friendship3) }
    }

    @Test
    fun `handleMemberProfileUpdated - success - updates only imageId`() {
        // Given
        val memberId = 1L
        val imageId = 777L

        val friendship = mockk<Friendship>(relaxed = true)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("imageId"),
            nickname = null,
            imageId = imageId
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns listOf(friendship)

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            friendship.updateMemberInfo(
                memberId = memberId,
                nickname = null,
                imageId = imageId
            )
        }
        verify(exactly = 1) { friendshipRepository.save(friendship) }
    }

    @Test
    fun `handleMemberProfileUpdated - success - handles no active friendships`() {
        // Given
        val memberId = 1L

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = "새닉네임",
            imageId = null
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns emptyList()

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) { friendshipRepository.findAllActiveByMemberId(memberId) }
        // save가 호출되지 않아야 함
        verify(exactly = 0) { friendshipRepository.save(any()) }
    }

    @Test
    fun `handleMemberProfileUpdated - success - handles empty update fields`() {
        // Given
        val memberId = 1L

        val friendship = mockk<Friendship>(relaxed = true)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = emptyList(),
            nickname = null,
            imageId = null
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns listOf(friendship)

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verify(exactly = 1) {
            friendship.updateMemberInfo(
                memberId = memberId,
                nickname = null,
                imageId = null
            )
        }
        verify(exactly = 1) { friendshipRepository.save(friendship) }
    }

    @Test
    fun `handleMemberProfileUpdated - success - preserves order of operations`() {
        // Given
        val memberId = 1L
        val nickname = "순서테스트"

        val friendship1 = mockk<Friendship>(relaxed = true)
        val friendship2 = mockk<Friendship>(relaxed = true)

        val event = MemberProfileUpdatedDomainEvent(
            memberId = memberId,
            email = "test@example.com",
            updatedFields = listOf("nickname"),
            nickname = nickname,
            imageId = null
        )

        every { friendshipRepository.findAllActiveByMemberId(memberId) } returns listOf(
            friendship1, friendship2
        )

        // When
        friendshipDomainEventListener.handleMemberProfileUpdated(event)

        // Then
        verifyOrder {
            friendshipRepository.findAllActiveByMemberId(memberId)

            friendship1.updateMemberInfo(memberId, nickname, null)
            friendshipRepository.save(friendship1)

            friendship2.updateMemberInfo(memberId, nickname, null)
            friendshipRepository.save(friendship2)
        }
    }
}
