package com.albert.realmoneyrealtaste.domain.member

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProfileAddressTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "User123",
            "사용자456",
            "User한글789",
            "가나다라마",
            "abcDEF123",
            "가나123ABC"
        ]
    )
    fun `test valid profile address`(addressValue: String) {
        val address = ProfileAddress(addressValue)

        assertEquals(addressValue, address.address)
    }

    @Test
    fun `test empty profile address`() {
        val address = ProfileAddress("")

        assertEquals("", address.address)
    }

    @Test
    fun `test invalid profile address - too short`() {
        val invalidAddress = "ab"

        assertFailsWith<IllegalArgumentException> {
            ProfileAddress(invalidAddress)
        }.let {
            assertEquals("프로필 주소는 3-15자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `test invalid profile address - too long`() {
        val invalidAddress = "a".repeat(16)

        assertFailsWith<IllegalArgumentException> {
            ProfileAddress(invalidAddress)
        }.let {
            assertEquals("프로필 주소는 3-15자 사이여야 합니다", it.message)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Invalid@Addr!",
            "user_name",
            "user.name",
            "user name",
            "user#123"
        ]
    )
    fun `test invalid profile address with special characters`(invalidAddress: String) {
        assertFailsWith<IllegalArgumentException> {
            ProfileAddress(invalidAddress)
        }.let {
            assertEquals("프로필 주소는 영문, 숫자, 한글만 사용 가능합니다", it.message)
        }
    }
}
