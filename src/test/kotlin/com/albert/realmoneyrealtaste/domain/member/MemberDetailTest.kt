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

        memberDetail.activate()

        assertEquals(true, memberDetail.activatedAt != null)
        assertEquals(null, memberDetail.deactivatedAt)
    }

    @Test
    fun `test deactivate MemberDetail`() {
        val memberDetail = MemberDetail.register(
            profileAddress = null,
            introduction = null
        )
        memberDetail.activate()

        memberDetail.deactivate()

        assertEquals(true, memberDetail.deactivatedAt != null)
    }

    @Test
    fun `test update MemberDetail info`() {
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
