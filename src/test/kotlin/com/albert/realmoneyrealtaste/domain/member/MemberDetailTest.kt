package com.albert.realmoneyrealtaste.domain.member

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberDetailTest {

    @Test
    fun `test register MemberDetail`() {
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "albert123"),
            introduction = Introduction(value = "Hello, I'm a test user.")
        )

        assertEquals("albert123", memberDetail.profileAddress?.address)
        assertEquals("Hello, I'm a test user.", memberDetail.introduction?.value)
        assertEquals(null, memberDetail.activatedAt)
        assertEquals(null, memberDetail.deactivatedAt)
        assertEquals(true, memberDetail.registeredAt <= LocalDateTime.now())
    }

    @Test
    fun `test activate MemberDetail`() {
        val memberDetail = MemberDetail.register(
            profileAddress = null,
            introduction = null
        )

        val activatedDetail = memberDetail.activate()

        assertEquals(true, activatedDetail.activatedAt != null)
        assertEquals(null, activatedDetail.deactivatedAt)
    }

    @Test
    fun `test deactivate MemberDetail`() {
        val memberDetail = MemberDetail.register(
            profileAddress = null,
            introduction = null
        ).activate()

        val deactivatedDetail = memberDetail.deactivate()

        assertEquals(true, deactivatedDetail.deactivatedAt != null)
    }

    @Test
    fun `test update MemberDetail info`() {
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = Introduction(value = "Old introduction")
        )

        val updatedDetail = memberDetail.updateInfo(
            profileAddress = ProfileAddress(address = "newAddress"),
            introduction = Introduction(value = "New introduction")
        )

        assertEquals("newAddress", updatedDetail.profileAddress?.address)
        assertEquals("New introduction", updatedDetail.introduction?.value)
    }
}
