package com.albert.realmoneyrealtaste.application.friend.dto

import com.albert.realmoneyrealtaste.config.TestPasswordEncoder
import com.albert.realmoneyrealtaste.domain.common.BaseEntity
import com.albert.realmoneyrealtaste.domain.friend.Friendship
import com.albert.realmoneyrealtaste.domain.friend.FriendshipStatus
import com.albert.realmoneyrealtaste.domain.friend.command.FriendRequestCommand
import com.albert.realmoneyrealtaste.domain.member.Member
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import java.lang.reflect.Field
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * FriendshipResponse DTO 매핑 테스트
 *
 * 이 테스트는 단순한 필드 매핑이 아닌, null profileImageId를 0L로 변환하는
 * 중요한 비즈니스 로직을 검증하기 위해 유지됩니다.
 *
 * 참고: 프로젝트의 통합 테스트 우선 철학에 따라, 이 단위 테스트는
 * FriendshipReadService 통합 테스트에서 커버되지 않는 특정 비즈니스 로직만 검증합니다.
 */
class FriendshipResponseTest {

    @Test
    @DisplayName("from - success - creates response with all friendship information")
    fun `from - success - creates response with all friendship information`() {
        val fromMemberId = 1L
        val member = Member.register(
            nickname = Nickname("sender"),
            email = Email("sender@example.com"),
            password = PasswordHash.of(RawPassword("Password1!"), TestPasswordEncoder()),
        )
        setMemberId(member, fromMemberId)
        setMemberImageId(member, 123L)

        val toMemberId = 2L
        val friend = Member.register(
            nickname = Nickname("receiver"),
            email = Email("receiver@example.com"),
            password = PasswordHash.of(RawPassword("Password1!"), TestPasswordEncoder()),
        )
        setMemberId(friend, toMemberId)
        setMemberImageId(friend, 456L)

        val command = FriendRequestCommand(fromMemberId, member.nickname.value, toMemberId, friend.nickname.value)
        val friendship = Friendship.request(command)

        // ID 설정 (실제로는 JPA가 수행)
        setFriendshipId(friendship, 100L)

        val result = FriendshipResponse.from(friendship, member, friend)

        assertAll(
            { assertEquals(100L, result.friendshipId) },
            { assertEquals(fromMemberId, result.memberId) },
            { assertEquals(toMemberId, result.friendMemberId) },
            { assertEquals("sender", result.memberNickname) },
            { assertEquals("receiver", result.friendNickname) },
            { assertEquals(123L, result.memberProfileImageId) },
            { assertEquals(456L, result.friendProfileImageId) },
            { assertEquals(FriendshipStatus.PENDING, result.status) },
            { assertNotNull(result.createdAt) },
            { assertNotNull(result.updatedAt) },
            { assertEquals(0, result.mutualFriendsCount) }
        )
    }

    @Test
    @DisplayName("from - success - handles null profile image ids as zero")
    fun `from - success - handles null profile image ids as zero`() {
        val fromMemberId = 1L
        val member = Member.register(
            nickname = Nickname("sender"),
            email = Email("sender@example.com"),
            password = PasswordHash.of(RawPassword("Password1!"), TestPasswordEncoder()),
        )
        setMemberId(member, fromMemberId)
        // profileImageId를 설정하지 않음 (null 상태 유지)

        val toMemberId = 2L
        val friend = Member.register(
            nickname = Nickname("receiver"),
            email = Email("receiver@example.com"),
            password = PasswordHash.of(RawPassword("Password1!"), TestPasswordEncoder()),
        )
        setMemberId(friend, toMemberId)
        // profileImageId를 설정하지 않음 (null 상태 유지)

        val command = FriendRequestCommand(fromMemberId, member.nickname.value, toMemberId, friend.nickname.value)
        val friendship = Friendship.request(command)
        setFriendshipId(friendship, 100L)

        val result = FriendshipResponse.from(friendship, member, friend)

        assertAll(
            { assertEquals(0L, result.memberProfileImageId) },
            { assertEquals(0L, result.friendProfileImageId) }
        )
    }

    private fun setFriendshipId(friendship: Friendship, id: Long) {
        val field: Field = BaseEntity::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(friendship, id)
    }

    private fun setStatus(friendship: Friendship, status: FriendshipStatus) {
        val field: Field = Friendship::class.java.getDeclaredField("status")
        field.isAccessible = true
        field.set(friendship, status)
    }

    private fun setMemberId(member: Member, memberId: Long) {
        val field: Field = BaseEntity::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(member, memberId)
    }

    private fun setMemberImageId(member: Member, imageId: Long) {
        val detailField: Field = Member::class.java.getDeclaredField("detail")
        detailField.isAccessible = true
        val detail = detailField.get(member)

        val imageIdField: Field = detail.javaClass.getDeclaredField("imageId")
        imageIdField.isAccessible = true
        imageIdField.set(detail, imageId)
    }
}
