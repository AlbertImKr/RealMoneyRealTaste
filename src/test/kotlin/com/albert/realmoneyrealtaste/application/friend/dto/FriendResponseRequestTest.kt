package com.albert.realmoneyrealtaste.application.friend.dto

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FriendResponseRequestTest {

    @Test
    fun `construct - success - creates FriendResponseRequest with valid IDs and accept true`() {
        val friendshipId = 1L
        val respondentMemberId = 2L
        val accept = true

        val request = FriendResponseRequest(friendshipId, respondentMemberId, accept)

        assertAll(
            { assertEquals(friendshipId, request.friendshipId) },
            { assertEquals(respondentMemberId, request.respondentMemberId) },
            { assertTrue(request.accept) }
        )
    }

    @Test
    fun `construct - success - creates FriendResponseRequest with valid IDs and accept false`() {
        val friendshipId = 1L
        val respondentMemberId = 2L
        val accept = false

        val request = FriendResponseRequest(friendshipId, respondentMemberId, accept)

        assertAll(
            { assertEquals(friendshipId, request.friendshipId) },
            { assertEquals(respondentMemberId, request.respondentMemberId) },
            { assertFalse(request.accept) }
        )
    }

    @Test
    fun `construct - failure - throws exception when friendshipId is zero`() {
        val friendshipId = 0L
        val respondentMemberId = 1L
        val accept = true

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            assertEquals(FriendResponseRequest.ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when friendshipId is negative`() {
        val friendshipId = -1L
        val respondentMemberId = 1L
        val accept = true

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            assertEquals(FriendResponseRequest.ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when respondentMemberId is zero`() {
        val friendshipId = 1L
        val respondentMemberId = 0L
        val accept = true

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            assertEquals(FriendResponseRequest.ERROR_RESPONDENT_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when respondentMemberId is negative`() {
        val friendshipId = 1L
        val respondentMemberId = -1L
        val accept = true

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            assertEquals(FriendResponseRequest.ERROR_RESPONDENT_MEMBER_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both friendshipId and respondentMemberId are zero`() {
        val friendshipId = 0L
        val respondentMemberId = 0L
        val accept = true

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            // friendshipId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendResponseRequest.ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE, it.message)
        }
    }

    @Test
    fun `construct - failure - throws exception when both friendshipId and respondentMemberId are negative`() {
        val friendshipId = -1L
        val respondentMemberId = -2L
        val accept = false

        assertFailsWith<IllegalArgumentException> {
            FriendResponseRequest(friendshipId, respondentMemberId, accept)
        }.let {
            // friendshipId 검증이 먼저 수행되므로 해당 에러 메시지가 나옴
            assertEquals(FriendResponseRequest.ERROR_FRIENDSHIP_ID_MUST_BE_POSITIVE, it.message)
        }
    }
}
