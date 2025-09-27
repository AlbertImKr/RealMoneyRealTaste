package com.albert.realmoneyrealtaste.domain.member

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberDetailTest {

    @Test
    fun `test register MemberDetail`() {
        val profileAddress = ProfileAddress(address = "albert123")
        val introduction = Introduction(value = "Hello, I'm a test user.")
        val memberDetail = MemberDetail.register(
            profileAddress = profileAddress,
            introduction = introduction
        )

        assertEquals(profileAddress, memberDetail.profileAddress)
        assertEquals(introduction, memberDetail.introduction)
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

        val newProfileAddress = ProfileAddress(address = "newAddress")
        val newIntroduction = Introduction(value = "New introduction")
        val updatedDetail = memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            introduction = newIntroduction
        )

        assertEquals(newProfileAddress, updatedDetail.profileAddress)
        assertEquals(newIntroduction, updatedDetail.introduction)
    }

    @Test
    fun `test register MemberDetail with null values`() {
        val memberDetail = MemberDetail.register()

        assertEquals(null, memberDetail.profileAddress)
        assertEquals(null, memberDetail.introduction)
        assertEquals(null, memberDetail.activatedAt)
        assertEquals(null, memberDetail.deactivatedAt)
        assertEquals(true, memberDetail.registeredAt <= LocalDateTime.now())
    }
}
