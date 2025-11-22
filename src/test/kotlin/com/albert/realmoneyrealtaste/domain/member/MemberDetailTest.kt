package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberDetailTest {

    @Test
    fun `register - success - creates member detail with profile address, introduction and address`() {
        val profileAddress = ProfileAddress(address = "albert123")
        val introduction = Introduction(value = "Hello, I'm a test user.")
        val address = "Seoul, South Korea"

        val memberDetail = MemberDetail.register(
            profileAddress = profileAddress,
            introduction = introduction,
            address = address
        )

        assertEquals(profileAddress, memberDetail.profileAddress)
        assertEquals(introduction, memberDetail.introduction)
        assertEquals(address, memberDetail.address)
        assertNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
        assertTrue(memberDetail.registeredAt <= LocalDateTime.now())
    }

    @Test
    fun `register - success - creates member detail with null values when no parameters`() {
        val memberDetail = MemberDetail.register()

        assertNull(memberDetail.profileAddress)
        assertNull(memberDetail.introduction)
        assertNull(memberDetail.address)
        assertNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
        assertTrue(memberDetail.registeredAt <= LocalDateTime.now())
    }

    @Test
    fun `activate - success - sets activated timestamp`() {
        val memberDetail = MemberDetail.register()

        memberDetail.activate()

        assertNotNull(memberDetail.activatedAt)
        assertNull(memberDetail.deactivatedAt)
    }

    @Test
    fun `deactivate - success - sets deactivated timestamp`() {
        val memberDetail = MemberDetail.register()
        memberDetail.activate()

        memberDetail.deactivate()

        assertNotNull(memberDetail.deactivatedAt)
    }

    @Test
    fun `updateInfo - success - updates profile address, introduction and address`() {
        // Given
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = Introduction(value = "Old introduction"),
            address = "oldAddress"
        )

        val newProfileAddress = ProfileAddress(address = "newAddress")
        val newIntroduction = Introduction(value = "New introduction")
        val newAddress = "newFullAddress"

        // When
        memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            introduction = newIntroduction,
            address = newAddress
        )

        // Then
        assertEquals(newProfileAddress, memberDetail.profileAddress)
        assertEquals(newIntroduction, memberDetail.introduction)
        assertEquals(newAddress, memberDetail.address)
    }

    @Test
    fun `updateInfo - success - updates only address when other fields are null`() {
        // Given
        val oldProfileAddress = ProfileAddress(address = "existingAddress")
        val oldIntroduction = Introduction(value = "Existing introduction")
        val memberDetail = MemberDetail.register(
            profileAddress = oldProfileAddress,
            introduction = oldIntroduction,
            address = "oldAddress"
        )

        val newAddress = "newFullAddress"

        // When
        memberDetail.updateInfo(address = newAddress)

        // Then
        assertEquals(oldProfileAddress, memberDetail.profileAddress, "Profile address should remain unchanged")
        assertEquals(oldIntroduction, memberDetail.introduction, "Introduction should remain unchanged")
        assertEquals(newAddress, memberDetail.address)
    }

    @Test
    fun `updateInfo - success - updates profile address and introduction when address is null`() {
        // Given
        val oldAddress = "existingFullAddress"
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = Introduction(value = "Old introduction"),
            address = oldAddress
        )

        val newProfileAddress = ProfileAddress(address = "newAddress")
        val newIntroduction = Introduction(value = "New introduction")

        // When
        memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            introduction = newIntroduction
        )

        // Then
        assertEquals(newProfileAddress, memberDetail.profileAddress)
        assertEquals(newIntroduction, memberDetail.introduction)
        assertEquals(oldAddress, memberDetail.address, "Address should remain unchanged")
    }

    @Test
    fun `updateInfo - success - updates profile address and address when introduction is null`() {
        // Given
        val oldIntroduction = Introduction(value = "Existing introduction")
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = oldIntroduction,
            address = "oldAddress"
        )

        val newProfileAddress = ProfileAddress(address = "newAddress")
        val newAddress = "newFullAddress"

        // When
        memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            address = newAddress
        )

        // Then
        assertEquals(newProfileAddress, memberDetail.profileAddress)
        assertEquals(oldIntroduction, memberDetail.introduction, "Introduction should remain unchanged")
        assertEquals(newAddress, memberDetail.address)
    }

    @Test
    fun `updateInfo - success - updates introduction and address when profile address is null`() {
        // Given
        val oldProfileAddress = ProfileAddress(address = "existingAddress")
        val memberDetail = MemberDetail.register(
            profileAddress = oldProfileAddress,
            introduction = Introduction(value = "Old introduction"),
            address = "oldAddress"
        )

        val newIntroduction = Introduction(value = "New introduction")
        val newAddress = "newFullAddress"

        // When
        memberDetail.updateInfo(
            introduction = newIntroduction,
            address = newAddress
        )

        // Then
        assertEquals(oldProfileAddress, memberDetail.profileAddress, "Profile address should remain unchanged")
        assertEquals(newIntroduction, memberDetail.introduction)
        assertEquals(newAddress, memberDetail.address)
    }

    @Test
    fun `updateInfo - success - updates nothing when all parameters are null`() {
        // Given
        val oldProfileAddress = ProfileAddress(address = "existingAddress")
        val oldIntroduction = Introduction(value = "Existing introduction")
        val oldAddress = "existingFullAddress"
        val memberDetail = MemberDetail.register(
            profileAddress = oldProfileAddress,
            introduction = oldIntroduction,
            address = oldAddress
        )

        // When
        memberDetail.updateInfo()

        // Then
        assertEquals(oldProfileAddress, memberDetail.profileAddress, "Profile address should remain unchanged")
        assertEquals(oldIntroduction, memberDetail.introduction, "Introduction should remain unchanged")
        assertEquals(oldAddress, memberDetail.address, "Address should remain unchanged")
    }

    @Test
    fun `updateInfo - success - updates only profile address when other fields are null`() {
        val oldIntroduction = Introduction(value = "Existing introduction")
        val oldAddress = "existingFullAddress"
        val memberDetail = MemberDetail.register(
            profileAddress = ProfileAddress(address = "oldAddress"),
            introduction = oldIntroduction,
            address = oldAddress,
        )

        val newProfileAddress = ProfileAddress(address = "newAddress")

        memberDetail.updateInfo(
            profileAddress = newProfileAddress,
            introduction = null,
            address = null
        )

        assertEquals(newProfileAddress, memberDetail.profileAddress)
        assertEquals(oldIntroduction, memberDetail.introduction, "Introduction should remain unchanged")
        assertEquals(oldAddress, memberDetail.address, "Address should remain unchanged")
    }

    @Test
    fun `updateInfo - success - updates only introduction when other fields are null`() {
        val oldProfileAddress = ProfileAddress(address = "existingAddress")
        val oldAddress = "existingFullAddress"
        val memberDetail = MemberDetail.register(
            profileAddress = oldProfileAddress,
            introduction = Introduction(value = "Old introduction"),
            address = oldAddress,
        )

        val newIntroduction = Introduction(value = "New introduction")

        memberDetail.updateInfo(
            profileAddress = null,
            introduction = newIntroduction,
            address = null
        )

        assertEquals(oldProfileAddress, memberDetail.profileAddress, "Profile address should remain unchanged")
        assertEquals(newIntroduction, memberDetail.introduction)
        assertEquals(oldAddress, memberDetail.address, "Address should remain unchanged")
    }

    @Test
    fun `updateInfo - success - keeps existing values when both parameters are null`() {
        val oldProfileAddress = ProfileAddress(address = "existingAddress")
        val oldIntroduction = Introduction(value = "Existing introduction")
        val memberDetail = MemberDetail.register(
            profileAddress = oldProfileAddress,
            introduction = oldIntroduction
        )

        memberDetail.updateInfo(profileAddress = null, introduction = null)

        assertEquals(oldProfileAddress, memberDetail.profileAddress, "Profile address should remain unchanged")
        assertEquals(oldIntroduction, memberDetail.introduction, "Introduction should remain unchanged")
    }

    @Test
    fun `updateInfo - success - handles null values for both parameters`() {
        val memberDetail = MemberDetail.register(profileAddress = null, introduction = null)

        memberDetail.updateInfo(profileAddress = null, introduction = null)

        assertNull(memberDetail.profileAddress, "Profile address should remain null")
        assertNull(memberDetail.introduction, "Introduction should remain null")
    }

    @Test
    fun `updateInfo - success - handles null values for all parameters`() {
        val memberDetail = MemberDetail.register(profileAddress = null, introduction = null)

        memberDetail.updateInfo(profileAddress = null, introduction = null, address = null)

        assertNull(memberDetail.profileAddress, "Profile address should remain null")
        assertNull(memberDetail.introduction, "Introduction should remain null")
        assertNull(memberDetail.address, "Address should remain null")
    }
}
