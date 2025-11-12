package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberDeactivateException
import com.albert.realmoneyrealtaste.application.member.exception.MemberUpdateException
import com.albert.realmoneyrealtaste.application.member.exception.PasswordChangeException
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.util.MemberFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberUpdaterTest(
    private val memberUpdater: MemberUpdater,
    private val memberRegister: MemberRegister,
    private val passwordEncoder: PasswordEncoder,
) : IntegrationTestBase() {

    @Test
    fun `updateInfo - success - updates member information`() {
        val member = registerAndActivateMember()
        val newNickname = Nickname("newNickname")
        val newProfileAddress = ProfileAddress("newAddress123")
        val newIntroduction = Introduction("Hello, I'm updated!")
        val memberId = member.id!!
        val request = AccountUpdateRequest(
            nickname = newNickname,
            profileAddress = newProfileAddress,
            introduction = newIntroduction,
        )

        val updatedMember = memberUpdater.updateInfo(memberId, request)

        assertEquals(newNickname, updatedMember.nickname)
        assertEquals(newProfileAddress, updatedMember.detail.profileAddress)
        assertEquals(newIntroduction, updatedMember.detail.introduction)
    }

    @Test
    fun `updateInfo - success - updates only nickname when other fields are null`() {
        val member = registerAndActivateMember()
        val originalProfileAddress = member.detail.profileAddress
        val originalIntroduction = member.detail.introduction
        val newNickname = Nickname("onlyNickname")
        val memberId = member.id!!
        val request = AccountUpdateRequest(
            nickname = newNickname,
            profileAddress = null,
            introduction = null,
        )

        val updatedMember = memberUpdater.updateInfo(memberId, request)

        assertEquals(newNickname, updatedMember.nickname)
        assertEquals(originalProfileAddress, updatedMember.detail.profileAddress)
        assertEquals(originalIntroduction, updatedMember.detail.introduction)
    }

    @Test
    fun `updateInfo - success - does not update when all fields are null`() {
        val member = registerAndActivateMember()
        val originalNickname = member.nickname
        val originalProfileAddress = member.detail.profileAddress
        val originalIntroduction = member.detail.introduction
        val memberId = member.id!!
        val request = AccountUpdateRequest(
            nickname = null,
            profileAddress = null,
            introduction = null,
        )

        val updatedMember = memberUpdater.updateInfo(memberId, request)

        assertEquals(originalNickname, updatedMember.nickname)
        assertEquals(originalProfileAddress, updatedMember.detail.profileAddress)
        assertEquals(originalIntroduction, updatedMember.detail.introduction)
    }

    @Test
    fun `updateInfo - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L
        val request = AccountUpdateRequest(
            nickname = Nickname("test"),
            profileAddress = null,
            introduction = null,
        )

        assertFailsWith<MemberUpdateException> {
            memberUpdater.updateInfo(nonExistentId, request)
        }
    }

    @Test
    fun `updateInfo - failure - throws exception when member is not active`() {
        val member = registerMember()
        val memberId = member.id!!
        val request = AccountUpdateRequest(
            nickname = Nickname("test"),
            profileAddress = null,
            introduction = null,
        )

        assertFailsWith<MemberUpdateException> {
            memberUpdater.updateInfo(memberId, request)
        }.let {
            assertEquals("회원 정보 업데이트 중 오류가 발생했습니다", it.message)
        }
    }

    @Test
    fun `updateInfo - failure - throws exception when profile address is duplicate`() {
        val member1 = registerAndActivateMember()
        val member2 = registerAndActivateMember(Email("member2@mail.com"), Nickname("member2"))
        val member1Id = member1.id!!
        val member2Id = member2.id!!
        val duplicateProfileAddress = ProfileAddress("duplicate")
        val request = AccountUpdateRequest(
            nickname = null,
            profileAddress = duplicateProfileAddress,
            introduction = null,
        )
        val member1UP = memberUpdater.updateInfo(member1Id, request)

        assertFailsWith<MemberUpdateException> {
            val member2UP = memberUpdater.updateInfo(member2Id, request)
            assertEquals(member1UP.detail.profileAddress, member2UP.detail.profileAddress)
        }.let {
            assertEquals("회원 정보 업데이트 중 오류가 발생했습니다", it.message)
        }
    }

    @Test
    fun `updatePassword - success - updates password`() {
        val member = registerAndActivateMember()
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        val updatedMember = memberUpdater.updatePassword(member.id!!, currentPassword, newPassword)

        assertTrue(updatedMember.verifyPassword(newPassword, passwordEncoder))
        assertFalse(updatedMember.verifyPassword(currentPassword, passwordEncoder))
    }

    @Test
    fun `updatePassword - failure - throws exception when current password is incorrect`() {
        val member = registerAndActivateMember()
        val wrongPassword = RawPassword("wrongPassword123!")
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        assertFailsWith<PasswordChangeException> {
            memberUpdater.updatePassword(member.id!!, wrongPassword, newPassword)
        }.let {
            assertEquals("비밀번호 변경 중 오류가 발생했습니다", it.message)
        }
    }

    @Test
    fun `updatePassword - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        assertFailsWith<PasswordChangeException> {
            memberUpdater.updatePassword(nonExistentId, currentPassword, newPassword)
        }
    }

    @Test
    fun `updatePassword - failure - throws exception when member is not active`() {
        val member = registerMember()
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        assertFailsWith<PasswordChangeException> {
            memberUpdater.updatePassword(member.id!!, currentPassword, newPassword)
        }.let {
            assertEquals("비밀번호 변경 중 오류가 발생했습니다", it.message)
        }
    }

    @Test
    fun `deactivate - success - deactivates member`() {
        val member = registerAndActivateMember()

        val deactivatedMember = memberUpdater.deactivate(member.id!!)

        assertEquals(MemberStatus.DEACTIVATED, deactivatedMember.status)
    }

    @Test
    fun `deactivate - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberDeactivateException> {
            memberUpdater.deactivate(nonExistentId)
        }
    }

    @Test
    fun `deactivate - failure - throws exception when member is not active`() {
        val member = registerMember()

        assertFailsWith<MemberDeactivateException> {
            memberUpdater.deactivate(member.id!!)
        }.let {
            assertEquals("회원 탈퇴 중 오류가 발생했습니다", it.message)
        }
    }

    @Test
    fun `deactivate - failure - throws exception when member is already deactivated`() {
        val member = registerAndActivateMember()
        memberUpdater.deactivate(member.id!!)

        assertFailsWith<MemberDeactivateException> {
            memberUpdater.deactivate(member.id!!)
        }.let {
            assertEquals("회원 탈퇴 중 오류가 발생했습니다", it.message)
        }
    }

    private fun registerMember(
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
    ) = memberRegister.register(
        MemberRegisterRequest(
            email = email,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = nickname
        )
    )

    private fun registerAndActivateMember(
        email: Email = MemberFixture.DEFAULT_EMAIL,
        nickname: Nickname = MemberFixture.DEFAULT_NICKNAME,
    ) = registerMember(email, nickname).also { it.activate() }
}
