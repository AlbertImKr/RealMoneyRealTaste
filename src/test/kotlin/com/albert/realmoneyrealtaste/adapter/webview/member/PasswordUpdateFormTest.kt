package com.albert.realmoneyrealtaste.adapter.webview.member

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordUpdateFormTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `constructor - success - creates form with default empty values`() {
        val form = PasswordUpdateForm()

        assertAll(
            { assertEquals("", form.currentPassword) },
            { assertEquals("", form.newPassword) },
            { assertEquals("", form.confirmNewPassword) }
        )
    }

    @Test
    fun `constructor - success - creates form with specific values`() {
        val currentPassword = "CurrentPass1!"
        val newPassword = "NewPassword1!"
        val confirmNewPassword = "NewPassword1!"

        val form = PasswordUpdateForm(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmNewPassword = confirmNewPassword
        )

        assertAll(
            { assertEquals(currentPassword, form.currentPassword) },
            { assertEquals(newPassword, form.newPassword) },
            { assertEquals(confirmNewPassword, form.confirmNewPassword) }
        )
    }

    @Test
    fun `validate - failure - detects short currentPassword`() {
        val form = PasswordUpdateForm(
            currentPassword = "Pass1!",
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "currentPassword" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", violation?.message)
    }

    @Test
    fun `validate - failure - detects long currentPassword`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPassword123!" + "a".repeat(10),
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "currentPassword" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", violation?.message)
    }

    @Test
    fun `validate - failure - detects currentPassword without letter`() {
        val form = PasswordUpdateForm(
            currentPassword = "12345678!",
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "currentPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects currentPassword without digit`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass!",
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "currentPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects currentPassword without special character`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1",
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "currentPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects short newPassword`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "New1!",
            confirmNewPassword = "New1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "newPassword" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", violation?.message)
    }

    @Test
    fun `validate - failure - detects long newPassword`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "NewPassword123!" + "a".repeat(10),
            confirmNewPassword = "NewPassword123!" + "a".repeat(10)
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "newPassword" }
        assertEquals("비밀번호는 8자 이상 20자 이하로 입력해주세요.", violation?.message)
    }

    @Test
    fun `validate - failure - detects newPassword without letter`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "12345678!",
            confirmNewPassword = "12345678!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "newPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects newPassword without digit`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "NewPassword!",
            confirmNewPassword = "NewPassword!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "newPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects newPassword without special character`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "NewPassword1",
            confirmNewPassword = "NewPassword1"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "newPassword" }
        assertEquals("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다", violation?.message)
    }

    @Test
    fun `validate - failure - detects blank confirmNewPassword`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "NewPassword1!",
            confirmNewPassword = ""
        )

        val violations = validator.validate(form)

        assertTrue(violations.isNotEmpty())
        val violation = violations.find { it.propertyPath.toString() == "confirmNewPassword" }
        assertEquals("비밀번호 확인은 필수 입력 항목입니다.", violation?.message)
    }

    @Test
    fun `validate - success - passes with valid form`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPass1!",
            newPassword = "NewPassword1!",
            confirmNewPassword = "NewPassword1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts all allowed special characters in currentPassword`() {
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')

        specialChars.forEach { char ->
            val form = PasswordUpdateForm(
                currentPassword = "Current1$char",
                newPassword = "NewPassword1!",
                confirmNewPassword = "NewPassword1!"
            )

            val violations = validator.validate(form)

            assertTrue(violations.isEmpty(), "특수문자 '$char' 가 포함된 현재 비밀번호가 유효하지 않음")
        }
    }

    @Test
    fun `validate - success - accepts all allowed special characters in newPassword`() {
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')

        specialChars.forEach { char ->
            val form = PasswordUpdateForm(
                currentPassword = "CurrentPass1!",
                newPassword = "NewPass1$char",
                confirmNewPassword = "NewPass1$char"
            )

            val violations = validator.validate(form)

            assertTrue(violations.isEmpty(), "특수문자 '$char' 가 포함된 새 비밀번호가 유효하지 않음")
        }
    }

    @Test
    fun `validate - success - accepts minimum length password`() {
        val form = PasswordUpdateForm(
            currentPassword = "Current1!",
            newPassword = "NewPass1!",
            confirmNewPassword = "NewPass1!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validate - success - accepts maximum length password`() {
        val form = PasswordUpdateForm(
            currentPassword = "CurrentPassword12!",
            newPassword = "NewPassword123456!",
            confirmNewPassword = "NewPassword123456!"
        )

        val violations = validator.validate(form)

        assertTrue(violations.isEmpty())
    }
}
