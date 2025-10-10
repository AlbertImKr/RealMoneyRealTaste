package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.AccountUpdateRequest
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateProfileAddressException
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.Introduction
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.Nickname
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import com.albert.realmoneyrealtaste.domain.member.ProfileAddress
import com.albert.realmoneyrealtaste.domain.member.RawPassword
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

        assertFailsWith<MemberNotFoundException> {
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

        assertFailsWith<IllegalArgumentException> {
            memberUpdater.updateInfo(memberId, request)
        }.let {
            assertEquals("등록 완료 상태에서만 정보 수정이 가능합니다", it.message)
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

        assertFailsWith<DuplicateProfileAddressException> {
            val member2UP = memberUpdater.updateInfo(member2Id, request)
            assertEquals(member1UP.detail.profileAddress, member2UP.detail.profileAddress)
        }.let {
            assertEquals("이미 사용 중인 프로필 주소입니다.", it.message)
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

        assertFailsWith<IllegalArgumentException> {
            memberUpdater.updatePassword(member.id!!, wrongPassword, newPassword)
        }.let {
            assertEquals("현재 비밀번호가 일치하지 않습니다", it.message)
        }
    }

    @Test
    fun `updatePassword - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        assertFailsWith<MemberNotFoundException> {
            memberUpdater.updatePassword(nonExistentId, currentPassword, newPassword)
        }
    }

    @Test
    fun `updatePassword - failure - throws exception when member is not active`() {
        val member = registerMember()
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD

        assertFailsWith<IllegalArgumentException> {
            memberUpdater.updatePassword(member.id!!, currentPassword, newPassword)
        }.let {
            assertEquals("등록 완료 상태에서만 비밀번호 변경이 가능합니다", it.message)
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

        assertFailsWith<MemberNotFoundException> {
            memberUpdater.deactivate(nonExistentId)
        }
    }

    @Test
    fun `deactivate - failure - throws exception when member is not active`() {
        val member = registerMember()

        assertFailsWith<IllegalArgumentException> {
            memberUpdater.deactivate(member.id!!)
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate - failure - throws exception when member is already deactivated`() {
        val member = registerAndActivateMember()
        memberUpdater.deactivate(member.id!!)

        assertFailsWith<IllegalArgumentException> {
            memberUpdater.deactivate(member.id!!)
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
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
