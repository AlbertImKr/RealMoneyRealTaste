package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.exceptions.ProfileAddressValidationException
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
    fun `constructor - success - creates profile address with valid value`(addressValue: String) {
        val address = ProfileAddress(addressValue)

        assertEquals(addressValue, address.address)
    }

    @Test
    fun `constructor - success - creates profile address with empty value`() {
        val emptyValue = ""

        val address = ProfileAddress(emptyValue)

        assertEquals(emptyValue, address.address)
    }

    @Test
    fun `constructor - failure - throws exception when profile address is too short`() {
        val invalidAddress = "ab"

        assertFailsWith<ProfileAddressValidationException.InvalidLength> {
            ProfileAddress(invalidAddress)
        }.let {
            assertEquals("프로필 주소는 3-15자 사이여야 합니다", it.message)
        }
    }

    @Test
    fun `constructor - failure - throws exception when profile address is too long`() {
        val invalidAddress = "a".repeat(16)

        assertFailsWith<ProfileAddressValidationException.InvalidLength> {
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
    fun `constructor - failure - throws exception when profile address contains special characters`(invalidAddress: String) {
        assertFailsWith<ProfileAddressValidationException.InvalidFormat> {
            ProfileAddress(invalidAddress)
        }.let {
            assertEquals("프로필 주소는 영문, 숫자, 한글만 사용 가능합니다", it.message)
        }
    }
}
