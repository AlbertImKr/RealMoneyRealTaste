package com.albert.realmoneyrealtaste.domain.member

import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidMemberStatusException
import com.albert.realmoneyrealtaste.domain.member.exceptions.InvalidPasswordException
import com.albert.realmoneyrealtaste.domain.member.exceptions.UnauthorizedRoleOperationException
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.domain.member.value.Introduction
import com.albert.realmoneyrealtaste.domain.member.value.Nickname
import com.albert.realmoneyrealtaste.domain.member.value.PasswordHash
import com.albert.realmoneyrealtaste.domain.member.value.ProfileAddress
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.domain.member.value.Role
import com.albert.realmoneyrealtaste.domain.member.value.Roles
import com.albert.realmoneyrealtaste.domain.member.value.TrustLevel
import com.albert.realmoneyrealtaste.util.MemberFixture
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberTest {

    @Test
    fun `register - success - creates member with initial status and trust score`() {
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val password = MemberFixture.DEFAULT_PASSWORD
        val now = LocalDateTime.now()

        val member = Member.register(email, nickname, password)

        assertEquals(email, member.email)
        assertEquals(nickname, member.nickname)
        assertEquals(MemberStatus.PENDING, member.status)
        assertEquals(0, member.trustScore.score)
        assertEquals(TrustLevel.BRONZE, member.trustScore.level)
        assertEquals(0, member.trustScore.realMoneyReviewCount)
        assertEquals(0, member.trustScore.adReviewCount)
        assertTrue(member.detail.registeredAt >= now)
    }

    @Test
    fun `activate - success - changes status to active and updates timestamp`() {
        val member = MemberFixture.createMember()
        val beforeUpdatedAt = member.updatedAt

        member.activate()

        assertEquals(MemberStatus.ACTIVE, member.status)
        assertTrue(beforeUpdatedAt < member.updatedAt)
    }

    @Test
    fun `activate - failure - throws exception when member is already active`() {
        val member = MemberFixture.createMember()
        member.activate()

        assertFailsWith<InvalidMemberStatusException> {
            member.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `activate - failure - throws exception when member is deactivated`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.deactivate()

        assertFailsWith<InvalidMemberStatusException> {
            member.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate - success - changes status to deactivated and updates timestamp`() {
        val member = MemberFixture.createMember()
        val beforeUpdateAt = member.updatedAt
        member.activate()

        member.deactivate()

        assertEquals(MemberStatus.DEACTIVATED, member.status)
        assertTrue(beforeUpdateAt <= member.updatedAt)
    }

    @Test
    fun `deactivate - failure - throws exception when member is already deactivated`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.deactivate()

        assertFailsWith<InvalidMemberStatusException> {
            member.deactivate()
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate - failure - throws exception when member is not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<InvalidMemberStatusException> {
            member.deactivate()
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
        }
    }

    @Test
    fun `verifyPassword - success - returns true when password matches`() {
        val member = MemberFixture.createMember()
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER

        val verifyResult = member.verifyPassword(password, encoder)

        assertTrue(verifyResult)
    }

    @Test
    fun `updateInfo - success - updates member information and timestamp`() {
        val member = MemberFixture.createMember()
        member.activate()
        val newNickname = Nickname("newNick")
        val newProfileAddress = ProfileAddress("address123")
        val newIntroduction = Introduction("Hello, I'm new here!")
        val beforeUpdateAt = member.updatedAt

        member.updateInfo(
            nickname = newNickname,
            profileAddress = newProfileAddress,
            introduction = newIntroduction
        )

        assertEquals(newNickname, member.nickname)
        assertEquals(newProfileAddress, member.detail.profileAddress)
        assertEquals(newIntroduction, member.detail.introduction)
        assertTrue(beforeUpdateAt <= member.updatedAt)
    }

    @Test
    fun `updateInfo - failure - throws exception when member is not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<InvalidMemberStatusException> {
            member.updateInfo(
                nickname = Nickname("newNick"),
                profileAddress = ProfileAddress("address123"),
                introduction = Introduction("Hello, I'm new here!")
            )
        }.let {
            assertEquals("등록 완료 상태에서만 정보 수정이 가능합니다", it.message)
        }
    }

    @Test
    fun `updateInfo - success - keeps existing values when no parameters provided`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeNickname = member.nickname
        val beforeProfileAddress = member.detail.profileAddress
        val beforeIntroduction = member.detail.introduction
        val beforeUpdateAt = member.updatedAt

        member.updateInfo()

        assertEquals(beforeNickname, member.nickname)
        assertEquals(beforeProfileAddress, member.detail.profileAddress)
        assertEquals(beforeIntroduction, member.detail.introduction)
        assertEquals(beforeUpdateAt, member.updatedAt)
    }

    @Test
    fun `updateInfo - success - updates only nickname when other parameters are null`() {
        val member = MemberFixture.createMember()
        member.activate()
        val newNickname = Nickname("newNick")
        val beforeProfileAddress = member.detail.profileAddress
        val beforeIntroduction = member.detail.introduction
        val beforeUpdateAt = member.updatedAt

        member.updateInfo(nickname = newNickname, profileAddress = null, introduction = null)

        assertEquals(newNickname, member.nickname)
        assertEquals(beforeProfileAddress, member.detail.profileAddress)
        assertEquals(beforeIntroduction, member.detail.introduction)
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `updateInfo - success - updates only profile address when other parameters are null`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeNickname = member.nickname
        val newProfileAddress = ProfileAddress("newAddress")
        val beforeIntroduction = member.detail.introduction
        val beforeUpdateAt = member.updatedAt

        member.updateInfo(nickname = null, profileAddress = newProfileAddress, introduction = null)

        assertEquals(beforeNickname, member.nickname)
        assertEquals(newProfileAddress, member.detail.profileAddress)
        assertEquals(beforeIntroduction, member.detail.introduction)
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `updateInfo - success - updates only introduction when other parameters are null`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeNickname = member.nickname
        val beforeProfileAddress = member.detail.profileAddress
        val newIntroduction = Introduction("new intro")
        val beforeUpdateAt = member.updatedAt

        member.updateInfo(nickname = null, profileAddress = null, introduction = newIntroduction)

        assertEquals(beforeNickname, member.nickname)
        assertEquals(beforeProfileAddress, member.detail.profileAddress)
        assertEquals(newIntroduction, member.detail.introduction)
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `updateInfo - success - updates multiple fields when provided`() {
        val member = MemberFixture.createMember()
        member.activate()
        val newNickname = Nickname("newNick")
        val newProfileAddress = ProfileAddress("newAddress")
        val beforeIntroduction = member.detail.introduction
        val beforeUpdateAt = member.updatedAt

        member.updateInfo(nickname = newNickname, profileAddress = newProfileAddress, introduction = null)

        assertEquals(newNickname, member.nickname)
        assertEquals(newProfileAddress, member.detail.profileAddress)
        assertEquals(beforeIntroduction, member.detail.introduction)
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `updateTrustScore - success - updates trust score and timestamp`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeUpdateAt = member.updatedAt
        val updatedTrustScore = TrustScore.create()
        updatedTrustScore.addRealMoneyReview()
        updatedTrustScore.addRealMoneyReview()
        updatedTrustScore.addAdReview()

        member.updateTrustScore(updatedTrustScore)

        assertEquals(2, member.trustScore.realMoneyReviewCount)
        assertEquals(1, member.trustScore.adReviewCount)
        assertEquals(11, member.trustScore.score)
        assertEquals(TrustLevel.BRONZE, member.trustScore.level)
        assertTrue(beforeUpdateAt <= member.updatedAt)
    }

    @Test
    fun `canWriteReview - success - returns true when member is active`() {
        val member = MemberFixture.createMember()
        member.activate()

        val canWriteReview = member.isActive()

        assertTrue(canWriteReview)
    }

    @Test
    fun `canWriteReview - success - returns false when member is not active`() {
        val member = MemberFixture.createMember()

        val canWriteReview = member.isActive()

        assertFalse(canWriteReview)
    }

    @Test
    fun `setters - success - update email and detail for code coverage`() {
        val member = TestMember()
        val newEmail = Email("test@email.com")
        val newDetail = MemberDetail.register(ProfileAddress("address"), Introduction("intro"))
        val newRoles = Roles.of(Role.USER, Role.MANAGER)

        member.setEmailForTest(newEmail)
        member.setDetailForTest(newDetail)
        member.setRolesForTest(newRoles)

        assertEquals(newEmail, member.email)
        assertEquals(newDetail, member.detail)
        assertTrue(member.hasRole(Role.USER))
        assertTrue(member.hasRole(Role.MANAGER))
    }

    @Test
    fun `grantRole - success - adds role and updates timestamp`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeUpdateAt = member.updatedAt

        member.grantRole(Role.MANAGER)

        assertTrue(member.hasRole(Role.MANAGER))
        assertTrue(beforeUpdateAt <= member.updatedAt)
    }

    @Test
    fun `grantRole - failure - throws exception when member is not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<UnauthorizedRoleOperationException> {
            member.grantRole(Role.MANAGER)
        }.let {
            assertEquals("등록 완료 상태에서만 권한 부여가 가능합니다", it.message)
        }
    }

    @Test
    fun `revokeRole - success - removes role and updates timestamp`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.MANAGER)
        val beforeUpdateAt = member.updatedAt

        member.revokeRole(Role.MANAGER)

        assertFalse(member.hasRole(Role.MANAGER))
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `revokeRole - failure - throws exception when member is not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<UnauthorizedRoleOperationException> {
            member.revokeRole(Role.USER)
        }.let {
            assertEquals("등록 완료 상태에서만 권한 회수가 가능합니다", it.message)
        }
    }

    @Test
    fun `canManage - success - returns true when member is active and has manager role`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.MANAGER)

        assertTrue(member.canManage())
    }

    @Test
    fun `canManage - success - returns false when member is not active`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.MANAGER)
        member.deactivate()

        assertFalse(member.canManage())
    }

    @Test
    fun `canManage - success - returns false when member does not have manager role`() {
        val member = MemberFixture.createMember()
        member.activate()

        assertFalse(member.canManage())
    }

    @Test
    fun `canAdministrate - success - returns true when member is active and has admin role`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.ADMIN)

        assertTrue(member.canAdministrate())
    }

    @Test
    fun `canAdministrate - failure - returns false when member is not active`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.ADMIN)
        member.deactivate()

        assertFalse(member.canAdministrate())
    }

    @Test
    fun `canAdministrate - success - returns false when member does not have admin role`() {
        val member = MemberFixture.createMember()
        member.activate()

        assertFalse(member.canAdministrate())
    }

    @Test
    fun `hasRole - success - returns true when member has specific role`() {
        val member = MemberFixture.createMember()
        member.activate()

        assertTrue(member.hasRole(Role.USER))
        assertFalse(member.hasRole(Role.MANAGER))
    }

    @Test
    fun `hasAnyRole - success - returns true when member has any of the specified roles`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.grantRole(Role.MANAGER)

        assertTrue(member.hasAnyRole(Role.USER, Role.MANAGER))
        assertTrue(member.hasAnyRole(Role.MANAGER, Role.ADMIN))
        assertFalse(member.hasAnyRole(Role.ADMIN))
    }

    @Test
    fun `registerManager - success - creates member with manager role`() {
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val password = MemberFixture.DEFAULT_PASSWORD

        val member = Member.registerManager(email, nickname, password)

        assertEquals(MemberStatus.PENDING, member.status)
        assertTrue(member.hasRole(Role.USER))
        assertTrue(member.hasRole(Role.MANAGER))
        assertFalse(member.hasRole(Role.ADMIN))
    }

    @Test
    fun `registerAdmin - success - creates member with admin role`() {
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val password = MemberFixture.DEFAULT_PASSWORD

        val member = Member.registerAdmin(email, nickname, password)

        assertEquals(MemberStatus.PENDING, member.status)
        assertTrue(member.hasRole(Role.USER))
        assertFalse(member.hasRole(Role.MANAGER))
        assertTrue(member.hasRole(Role.ADMIN))
    }

    @Test
    fun `changePassword with current password - success - updates password when current password matches`() {
        val member = MemberFixture.createMember()
        member.activate()
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER
        val beforeUpdateAt = member.updatedAt

        member.changePassword(currentPassword, newPassword, encoder)

        assertTrue(member.verifyPassword(newPassword, encoder))
        assertFalse(member.verifyPassword(currentPassword, encoder))
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    @Test
    fun `changePassword with current password - failure - throws exception when current password does not match`() {
        val member = MemberFixture.createMember()
        member.activate()
        val wrongPassword = RawPassword("wrongPassword123!")
        val newPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER

        assertFailsWith<InvalidPasswordException> {
            member.changePassword(wrongPassword, newPassword, encoder)
        }.let {
            assertEquals("현재 비밀번호가 일치하지 않습니다", it.message)
        }
    }

    @Test
    fun `changePassword with current password - failure - throws exception when member is not active`() {
        val member = MemberFixture.createMember()
        val currentPassword = MemberFixture.DEFAULT_RAW_PASSWORD
        val newPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER

        assertFailsWith<InvalidMemberStatusException> {
            member.changePassword(currentPassword, newPassword, encoder)
        }.let {
            assertEquals("등록 완료 상태에서만 비밀번호 변경이 가능합니다", it.message)
        }
    }

    @Test
    fun `changePassword without current password - success - updates password and timestamp`() {
        val member = MemberFixture.createMember()
        val newPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER
        val beforeUpdateAt = member.updatedAt

        member.changePassword(PasswordHash.of(newPassword, encoder))

        assertTrue(member.verifyPassword(newPassword, encoder))
        assertTrue(beforeUpdateAt < member.updatedAt)
    }

    private class TestMember : Member(
        email = MemberFixture.DEFAULT_EMAIL,
        nickname = MemberFixture.DEFAULT_NICKNAME,
        passwordHash = MemberFixture.DEFAULT_PASSWORD,
        status = MemberStatus.PENDING,
        detail = MemberDetail.register(null, null),
        trustScore = TrustScore.create(),
        updatedAt = LocalDateTime.now(),
        roles = Roles.ofUser(),
    ) {
        fun setEmailForTest(email: Email) {
            this.email = email
        }

        fun setDetailForTest(detail: MemberDetail) {
            this.detail = detail
        }

        fun setRolesForTest(roles: Roles) {
            this.roles = roles
        }
    }
}
