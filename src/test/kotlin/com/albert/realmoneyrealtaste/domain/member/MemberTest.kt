package com.albert.realmoneyrealtaste.domain.member

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

        val activatedMember = member.activate()

        assertEquals(MemberStatus.ACTIVE, activatedMember.status)
        assertEquals(true, member.updatedAt <= activatedMember.updatedAt)
    }

    @Test
    fun `activate member when already active`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        assertFailsWith<IllegalArgumentException> {
            activatedMember.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `activate member when deactivated`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()
        val deactivatedMember = activatedMember.deactivate()

        assertFailsWith<IllegalArgumentException> {
            deactivatedMember.activate()
        }.let {
            assertEquals("등록 대기 상태에서만 등록 완료가 가능합니다", it.message)
        }
    }

    @Test
    fun `deactivate member`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        val deactivatedMember = activatedMember.deactivate()

        assertEquals(MemberStatus.DEACTIVATED, deactivatedMember.status)
        assertEquals(true, activatedMember.updatedAt <= deactivatedMember.updatedAt)
    }

    @Test
    fun `deactivate member when already deactivated`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()
        val deactivatedMember = activatedMember.deactivate()

        assertFailsWith<IllegalArgumentException> {
            deactivatedMember.deactivate()
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
        val activatedMember = member.activate()
        val rawNewPassword = MemberFixture.NEW_RAW_PASSWORD
        val encoder = MemberFixture.TEST_ENCODER
        val newPassword = MemberFixture.NEW_PASSWORD

        val updatedMember = activatedMember.changePassword(newPassword)

        assertEquals(true, updatedMember.verifyPassword(rawNewPassword, encoder))
        assertEquals(true, activatedMember.updatedAt <= updatedMember.updatedAt)
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
        val activatedMember = member.activate()

        val newNickname = Nickname("newNick")
        val newProfileAddress = ProfileAddress("address123")
        val newIntroduction = Introduction("Hello, I'm new here!")
        val updatedMember = activatedMember.updateInfo(
            nickname = newNickname,
            profileAddress = newProfileAddress,
            introduction = newIntroduction
        )

        assertEquals(newNickname, updatedMember.nickname)
        assertEquals(newProfileAddress, updatedMember.detail.profileAddress)
        assertEquals(newIntroduction, updatedMember.detail.introduction)
        assertEquals(true, activatedMember.updatedAt <= updatedMember.updatedAt)
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
        val activatedMember = member.activate()

        val updatedMember = activatedMember.updateInfo()

        assertEquals(activatedMember.nickname, updatedMember.nickname)
        assertEquals(activatedMember.detail.profileAddress, updatedMember.detail.profileAddress)
        assertEquals(activatedMember.detail.introduction, updatedMember.detail.introduction)
    }

    @Test
    fun `update trust score`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        val updatedTrustScore = TrustScore.create()
            .addRealMoneyReview()
            .addRealMoneyReview()
            .addAdReview()

        val updatedMember = activatedMember.updateTrustScore(updatedTrustScore)

        assertEquals(2, updatedMember.trustScore.realMoneyReviewCount)
        assertEquals(1, updatedMember.trustScore.adReviewCount)
        assertEquals(11, updatedMember.trustScore.score)
        assertEquals(TrustLevel.BRONZE, updatedMember.trustScore.level)
    }

    @Test
    fun `can write review`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        val canWriteReview = activatedMember.canWriteReview()

        assertEquals(true, canWriteReview)
    }

    @Test
    fun `cannot write review when not active`() {
        val member = MemberFixture.createMember()

        val canWriteReview = member.canWriteReview()

        assertEquals(false, canWriteReview)
    }
}
