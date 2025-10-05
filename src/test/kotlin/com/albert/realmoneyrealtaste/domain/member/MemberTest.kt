package com.albert.realmoneyrealtaste.domain.member

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MemberTest {

    @Test
    fun `register member`() {
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
        assertEquals(true, member.detail.registeredAt >= now)
    }

    @Test
    fun `activate member`() {
        val member = MemberFixture.createMember()
        val beforeUpdatedAt = member.updatedAt

        member.activate()

        assertEquals(MemberStatus.ACTIVE, member.status)
        assertEquals(true, beforeUpdatedAt < member.updatedAt)
    }

    @Test
    fun `activate member when already active`() {
        val member = MemberFixture.createMember()
        member.activate()

        assertFailsWith<IllegalArgumentException> {
            member.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `activate member when deactivated`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.deactivate()

        assertFailsWith<IllegalArgumentException> {
            member.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate member`() {
        val member = MemberFixture.createMember()
        val beforeUpdateAt = member.updatedAt
        member.activate()

        member.deactivate()

        assertEquals(MemberStatus.DEACTIVATED, member.status)
        assertTrue { beforeUpdateAt <= member.updatedAt }
    }

    @Test
    fun `deactivate member when already deactivated`() {
        val member = MemberFixture.createMember()
        member.activate()
        member.deactivate()

        assertFailsWith<IllegalArgumentException> {
            member.deactivate()
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate member when not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<IllegalArgumentException> {
            member.deactivate()
        }.let {
            assertEquals("등록 완료 상태에서만 탈퇴가 가능합니다", it.message)
        }
    }

    @Test
    fun `verify password`() {
        val member = MemberFixture.createMember()
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER

        val verifyResult = member.verifyPassword(password, encoder)

        assertEquals(true, verifyResult)
    }

    @Test
    fun `change password`() {
        val member = MemberFixture.createMember()
        member.activate()
        val beforeUpdateAt = member.updatedAt
        val rawNewPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER
        val newPassword = MemberFixture.NEW_PASSWORD

        member.changePassword(newPassword)

        assertEquals(true, member.verifyPassword(rawNewPassword, encoder))
        assertTrue { beforeUpdateAt <= member.updatedAt }
    }

    @Test
    fun `change password when not active`() {
        val member = MemberFixture.createMember()
        val newPassword = MemberFixture.NEW_PASSWORD

        assertFailsWith<IllegalArgumentException> {
            member.changePassword(newPassword)
        }.let {
            assertEquals("등록 완료 상태에서만 비밀번호 변경이 가능합니다", it.message)
        }
    }

    @Test
    fun `update info`() {
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
        assertTrue { beforeUpdateAt <= member.updatedAt }
    }

    @Test
    fun `update info when not active`() {
        val member = MemberFixture.createMember()

        assertFailsWith<IllegalArgumentException> {
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
    fun `update info with null values`() {
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
        assertTrue { beforeUpdateAt <= member.updatedAt }
    }

    @Test
    fun `update trust score`() {
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
        assertTrue { beforeUpdateAt <= member.updatedAt }
    }

    @Test
    fun `can write review`() {
        val member = MemberFixture.createMember()
        member.activate()

        val canWriteReview = member.canWriteReview()

        assertEquals(true, canWriteReview)
    }

    @Test
    fun `cannot write review when not active`() {
        val member = MemberFixture.createMember()

        val canWriteReview = member.canWriteReview()

        assertEquals(false, canWriteReview)
    }

    @Test
    fun `test setters for code coverage`() {
        val member = TestMember()
        val newEmail = Email("test@email.com")
        val newDetail = MemberDetail.register(ProfileAddress("address"), Introduction("intro"))

        member.setEmailForTest(newEmail)
        member.setDetailForTest(newDetail)

        assertEquals(newEmail, member.email)
        assertEquals(newDetail, member.detail)
    }

    private class TestMember() : Member(
        email = MemberFixture.DEFAULT_EMAIL,
        nickname = MemberFixture.DEFAULT_NICKNAME,
        passwordHash = MemberFixture.DEFAULT_PASSWORD,
        status = MemberStatus.PENDING,
        detail = MemberDetail.register(null, null),
        trustScore = TrustScore.create(),
        updatedAt = LocalDateTime.now()
    ) {
        fun setEmailForTest(email: Email) {
            this.email = email
        }

        fun setDetailForTest(detail: MemberDetail) {
            this.detail = detail
        }
    }
}
