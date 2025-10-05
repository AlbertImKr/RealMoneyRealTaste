package com.albert.realmoneyrealtaste.domain.member

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberDetailTest {

    @Test
    fun `register - success - creates member detail with profile address and introduction`() {
        val profileAddress = ProfileAddress(address = "albert123")
        val introduction = Introduction(value = "Hello, I'm a test user.")

        val memberDetail = MemberDetail.register(
            profileAddress = profileAddress,
            introduction = introduction
        )

        assertEquals(profileAddress, memberDetail.profileAddress)
        assertEquals(introduction, memberDetail.introduction)
        assertNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
        assertTrue(memberDetail.registeredAt <= LocalDateTime.now())
    }

    @Test
    fun `register - success - creates member detail with null values when no parameters`() {
        val memberDetail = MemberDetail.register()

        assertNull(memberDetail.profileAddress)
        assertNull(memberDetail.introduction)
        assertNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
        assertTrue(memberDetail.registeredAt <= LocalDateTime.now())
    }

    @Test
    fun `activate - success - sets activated timestamp`() {
        val memberDetail = MemberDetail.register(
            profileAddress = null,
            introduction = null
        )

        memberDetail.activate()

        assertNotNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
    }

    @Test
    fun `deactivate - success - sets deactivated timestamp`() {
        val memberDetail = MemberDetail.register(
            profileAddress = null,
            introduction = null
        )
        memberDetail.activate()

        memberDetail.deactivate()

        assertNotNull(memberDetail.deactivatedAt)
    }

    @Test
    fun `updateInfo - success - updates profile address and introduction`() {
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = Introduction(value = "Old introduction")
        )
        val newProfileAddress = ProfileAddress(address = "newAddress")
        val newIntroduction = Introduction(value = "New introduction")

        memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            introduction = newIntroduction
        )

        assertEquals(newProfileAddress, memberDetail.profileAddress)
        assertEquals(newIntroduction, memberDetail.introduction)
    }
}
