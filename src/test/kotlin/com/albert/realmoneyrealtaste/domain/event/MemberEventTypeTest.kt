package com.albert.realmoneyrealtaste.domain.event

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberEventTypeTest {

    @Test
    fun `enum values - success - contains all expected event types`() {
        val expectedTypes = listOf(
            // 친구 관련
            "FRIEND_REQUEST_SENT",
            "FRIEND_REQUEST_RECEIVED",
            "FRIEND_REQUEST_ACCEPTED",
            "FRIEND_REQUEST_REJECTED",
            "FRIENDSHIP_TERMINATED",

            // 게시물 관련
            "POST_CREATED",
            "POST_DELETED",
            "POST_COMMENTED",

            // 댓글 관련
            "COMMENT_CREATED",
            "COMMENT_DELETED",
            "COMMENT_REPLIED",

            // 프로필 관련
            "PROFILE_UPDATED",

            // 시스템 관련
            "ACCOUNT_ACTIVATED",
            "ACCOUNT_DEACTIVATED"
        )

        val actualTypes = MemberEventType.values().map { it.name }

        expectedTypes.forEach { expected ->
            assertTrue(actualTypes.contains(expected), "Missing enum value: $expected")
        }
    }

    @Test
    fun `enum values - success - friend related events are grouped together`() {
        val friendEvents = listOf(
            MemberEventType.FRIEND_REQUEST_SENT,
            MemberEventType.FRIEND_REQUEST_RECEIVED,
            MemberEventType.FRIEND_REQUEST_ACCEPTED,
            MemberEventType.FRIEND_REQUEST_REJECTED,
            MemberEventType.FRIENDSHIP_TERMINATED
        )

        friendEvents.forEach { eventType ->
            assertTrue(eventType.name.startsWith("FRIEND") || eventType.name.startsWith("FRIENDSHIP"))
        }
    }

    @Test
    fun `enum values - success - post related events are grouped together`() {
        val postEvents = listOf(
            MemberEventType.POST_CREATED,
            MemberEventType.POST_DELETED,
            MemberEventType.POST_COMMENTED
        )

        postEvents.forEach { eventType ->
            assertTrue(eventType.name.startsWith("POST"))
        }
    }

    @Test
    fun `enum values - success - comment related events are grouped together`() {
        val commentEvents = listOf(
            MemberEventType.COMMENT_CREATED,
            MemberEventType.COMMENT_DELETED,
            MemberEventType.COMMENT_REPLIED
        )

        commentEvents.forEach { eventType ->
            assertTrue(eventType.name.startsWith("COMMENT"))
        }
    }

    @Test
    fun `enum values - success - system related events are grouped together`() {
        val systemEvents = listOf(
            MemberEventType.ACCOUNT_ACTIVATED,
            MemberEventType.ACCOUNT_DEACTIVATED
        )

        systemEvents.forEach { eventType ->
            assertTrue(eventType.name.startsWith("ACCOUNT"))
        }
    }

    @Test
    fun `valueOf - success - returns correct enum for valid names`() {
        assertEquals(MemberEventType.FRIEND_REQUEST_SENT, MemberEventType.valueOf("FRIEND_REQUEST_SENT"))
        assertEquals(MemberEventType.POST_CREATED, MemberEventType.valueOf("POST_CREATED"))
        assertEquals(MemberEventType.COMMENT_CREATED, MemberEventType.valueOf("COMMENT_CREATED"))
        assertEquals(MemberEventType.PROFILE_UPDATED, MemberEventType.valueOf("PROFILE_UPDATED"))
        assertEquals(MemberEventType.ACCOUNT_ACTIVATED, MemberEventType.valueOf("ACCOUNT_ACTIVATED"))
    }

    @Test
    fun `enum constants - success - all constants are accessible`() {
        // 친구 관련
        assertEquals(MemberEventType.FRIEND_REQUEST_SENT.name, "FRIEND_REQUEST_SENT")
        assertEquals(MemberEventType.FRIEND_REQUEST_RECEIVED.name, "FRIEND_REQUEST_RECEIVED")
        assertEquals(MemberEventType.FRIEND_REQUEST_ACCEPTED.name, "FRIEND_REQUEST_ACCEPTED")
        assertEquals(MemberEventType.FRIEND_REQUEST_REJECTED.name, "FRIEND_REQUEST_REJECTED")
        assertEquals(MemberEventType.FRIENDSHIP_TERMINATED.name, "FRIENDSHIP_TERMINATED")

        // 게시물 관련
        assertEquals(MemberEventType.POST_CREATED.name, "POST_CREATED")
        assertEquals(MemberEventType.POST_DELETED.name, "POST_DELETED")
        assertEquals(MemberEventType.POST_COMMENTED.name, "POST_COMMENTED")

        // 댓글 관련
        assertEquals(MemberEventType.COMMENT_CREATED.name, "COMMENT_CREATED")
        assertEquals(MemberEventType.COMMENT_DELETED.name, "COMMENT_DELETED")
        assertEquals(MemberEventType.COMMENT_REPLIED.name, "COMMENT_REPLIED")

        // 프로필 관련
        assertEquals(MemberEventType.PROFILE_UPDATED.name, "PROFILE_UPDATED")

        // 시스템 관련
        assertEquals(MemberEventType.ACCOUNT_ACTIVATED.name, "ACCOUNT_ACTIVATED")
        assertEquals(MemberEventType.ACCOUNT_DEACTIVATED.name, "ACCOUNT_DEACTIVATED")
    }

    @Test
    fun `enum count - success - has correct number of values`() {
        val totalExpected = 14 // 5 friend + 3 post + 3 comment + 1 profile + 2 system
        assertEquals(totalExpected, MemberEventType.values().size)
    }
}
