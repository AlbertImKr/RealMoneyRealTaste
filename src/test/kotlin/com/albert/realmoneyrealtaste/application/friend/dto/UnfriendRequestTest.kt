package com.albert.realmoneyrealtaste.application.friend.dto

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnfriendRequestTest {

    @Test
    fun `construct - success - creates UnfriendRequest with valid member IDs`() {
        val memberId = 1L
        val friendMemberId = 2L

        val request = UnfriendRequest(memberId, friendMemberId)

        assertAll(
            { assertEquals(memberId, request.memberId) },
            { assertEquals(friendMemberId, request.friendMemberId) }
        )
    }

    @Test
    fun `construct - failure - throws exception when memberId is zero`() {
        val memberId = 0L
        val friendMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            assertEquals(UnfriendRequest.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when memberId is negative`() {
        val memberId = -1L
        val friendMemberId = 2L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            assertEquals(UnfriendRequest.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when friendMemberId is zero`() {
        val memberId = 1L
        val friendMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            assertEquals(UnfriendRequest.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when friendMemberId is negative`() {
        val memberId = 1L
        val friendMemberId = -1L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            assertEquals(UnfriendRequest.ERROR_FRIEND_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when memberId and friendMemberId are same`() {
        val memberId = 1L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, memberId)
        }.let {
            assertEquals(UnfriendRequest.ERROR_CANNOT_UNFRIEND_SELF, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both member IDs are zero`() {
        val memberId = 0L
        val friendMemberId = 0L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            // memberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(UnfriendRequest.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both member IDs are negative`() {
        val memberId = -1L
        val friendMemberId = -2L

        assertFailsWith<IllegalArgumentException> {
            UnfriendRequest(memberId, friendMemberId)
        }.let {
            // memberId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(UnfriendRequest.ERROR_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }
}
