package com.albert.realmoneyrealtaste.adapter.webview.member

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountUpdateFormTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `constructor - success - creates form with null values`() {
        val form = AccountUpdateForm(
            nickname = null,
            profileAddress = null,
            introduction = null
        )

        assertAll(
            { assertNull(form.nickname) },
            { assertNull(form.profileAddress) },
            { assertNull(form.introduction) }
        )
    }

    @Test
    fun `constructor - success - creates form with specific values`() {
        val nickname = "testNickname"
        val profileAddress = "testAddress"
        val introduction = "test introduction"

        val form = AccountUpdateForm(
            nickname = nickname,
            profileAddress = profileAddress,
            introduction = introduction
        )

        assertAll(
            { assertEquals(nickname, form.nickname) },
            { assertEquals(profileAddress, form.profileAddress) },
            { assertEquals(introduction, form.introduction) }
        )
    }

    @Test
    fun `validate - failure - detects blank nickname`() {
        val form = AccountUpdateForm(
            nickname = "",
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertNotNull(nicknameViolation)
    }

    @Test
    fun `validate - failure - detects nickname shorter than minimum length`() {
        val form = AccountUpdateForm(
            nickname = "a",
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임은 2자 이상 20자 이하로 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate - failure - detects nickname longer than maximum length`() {
        val form = AccountUpdateForm(
            nickname = "a".repeat(21),
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임은 2자 이상 20자 이하로 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate - success - accepts nickname at minimum length`() {
        val form = AccountUpdateForm(
            nickname = "ab",
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts nickname at maximum length`() {
        val form = AccountUpdateForm(
            nickname = "a".repeat(20),
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - failure - detects profileAddress shorter than minimum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "ab",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val addressViolation = violations.find { it.propertyPath.toString() == "profileAddress" }
        assertEquals("프로필 주소는 3자 이상 15자 이하로 입력해주세요.", addressViolation?.message)
    }

    @Test
    fun `validate - failure - detects profileAddress longer than maximum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "a".repeat(16),
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val addressViolation = violations.find { it.propertyPath.toString() == "profileAddress" }
        assertEquals("프로필 주소는 3자 이상 15자 이하로 입력해주세요.", addressViolation?.message)
    }

    @Test
    fun `validate - success - accepts profileAddress at minimum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "abc",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts profileAddress at maximum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "a".repeat(15),
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - failure - detects empty profileAddress`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val addressViolation = violations.find { it.propertyPath.toString() == "profileAddress" }
        assertEquals("프로필 주소는 3자 이상 15자 이하로 입력해주세요.", addressViolation?.message)
    }

    @Test
    fun `validate - failure - detects introduction longer than maximum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "address",
            introduction = "a".repeat(501)
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val introViolation = violations.find { it.propertyPath.toString() == "introduction" }
        assertEquals("소개글은 최대 500자까지 입력 가능합니다.", introViolation?.message)
    }

    @Test
    fun `validate - success - accepts introduction at maximum length`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "address",
            introduction = "a".repeat(500)
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts empty introduction`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "address",
            introduction = ""
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - failure - detects multiple errors`() {
        val form = AccountUpdateForm(
            nickname = "a",
            profileAddress = "ab",
            introduction = "a".repeat(501)
        )

        val violations = validator.validate(form)

        assertTrue(violations.size >= 3)
        assertTrue(violations.any { it.propertyPath.toString() == "nickname" })
        assertTrue(violations.any { it.propertyPath.toString() == "profileAddress" })
        assertTrue(violations.any { it.propertyPath.toString() == "introduction" })
    }

    @Test
    fun `validate - success - passes with valid form`() {
        val form = AccountUpdateForm(
            nickname = "validNickname",
            profileAddress = "validAddress",
            introduction = "This is a valid introduction"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - passes with only nickname provided`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = null,
            introduction = null
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - failure - detects null nickname`() {
        val form = AccountUpdateForm(
            nickname = null,
            profileAddress = null,
            introduction = null
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임을 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `toAccountUpdateRequest - success - converts with all values`() {
        val form = AccountUpdateForm(
            nickname = "testNickname",
            profileAddress = "testAddress",
            introduction = "test introduction"
        )

        val request = form.toAccountUpdateRequest()

        assertAll(
            { assertNotNull(request.nickname) },
            { assertEquals("testNickname", request.nickname?.value) },
            { assertNotNull(request.profileAddress) },
            { assertEquals("testAddress", request.profileAddress?.address) },
            { assertNotNull(request.introduction) },
            { assertEquals("test introduction", request.introduction?.value) }
        )
    }

    @Test
    fun `toAccountUpdateRequest - success - converts with null values`() {
        val form = AccountUpdateForm(
            nickname = null,
            profileAddress = null,
            introduction = null
        )

        val request = form.toAccountUpdateRequest()

        assertAll(
            { assertNull(request.nickname) },
            { assertNull(request.profileAddress) },
            { assertNull(request.introduction) }
        )
    }

    @Test
    fun `toAccountUpdateRequest - success - converts with partial values`() {
        val form = AccountUpdateForm(
            nickname = "testNickname",
            profileAddress = null,
            introduction = "test introduction"
        )

        val request = form.toAccountUpdateRequest()

        assertAll(
            { assertNotNull(request.nickname) },
            { assertEquals("testNickname", request.nickname?.value) },
            { assertNull(request.profileAddress) },
            { assertNotNull(request.introduction) },
            { assertEquals("test introduction", request.introduction?.value) }
        )
    }

    @Test
    fun `validate - success - accepts Korean characters in nickname`() {
        val form = AccountUpdateForm(
            nickname = "한글닉네임",
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts special characters in introduction`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "address",
            introduction = "안녕하세요! @#$%^&*() 특수문자가 포함된 소개글입니다."
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts alphanumeric profileAddress`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "user123",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts whitespace only nickname fails`() {
        val form = AccountUpdateForm(
            nickname = "   ",
            profileAddress = "address",
            introduction = "intro"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val nicknameViolation = violations.find { it.propertyPath.toString() == "nickname" }
        assertEquals("닉네임을 입력해주세요.", nicknameViolation?.message)
    }

    @Test
    fun `validate - success - multiline introduction is accepted`() {
        val form = AccountUpdateForm(
            nickname = "nickname",
            profileAddress = "address",
            introduction = "첫 번째 줄\n두 번째 줄\n세 번째 줄"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }
}
