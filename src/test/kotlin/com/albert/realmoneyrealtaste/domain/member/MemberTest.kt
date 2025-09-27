package com.albert.realmoneyrealtaste.domain.member

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemberTest {

    @Test
    fun `test register member`() {
        val email = Email("example123@example.com")
        val nickname = Nickname("exampleNick")
        val password = "securePassword"
        val passwordEncoder = BCryptPasswordEncoder()

        val member = Member.register(email, nickname, password, passwordEncoder)

        assertEquals(email, member.email)
        assertEquals(nickname, member.nickname)
        assert(passwordEncoder.matches(password, member.passwordHash))
        assertEquals(MemberStatus.PENDING, member.status)
        assertEquals(0, member.trustScore.score)
        assertEquals(TrustLevel.BRONZE, member.trustScore.level)
        assertEquals(0, member.trustScore.realMoneyReviewCount)
        assertEquals(0, member.trustScore.adReviewCount)
    }

    @Test
    fun `test activate member`() {
        val member = MemberFixture.createMember()

        val activatedMember = member.activate()

        assertEquals(MemberStatus.ACTIVE, activatedMember.status)
    }

    @Test
    fun `test deactivate member`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        val deactivatedMember = activatedMember.deactivate()

        assertEquals(MemberStatus.DEACTIVATED, deactivatedMember.status)
    }

    @Test
    fun `test verify password`() {
        val member = MemberFixture.createMember()
        val password = MemberFixture.DEFAULT_PASSWORD
        val passwordEncoder = MemberFixture.DEFAULT_PASSWORD_ENCODER

        val verifyResult = member.verifyPassword(password, passwordEncoder)

        assertEquals(true, verifyResult)
    }

    @Test
    fun `test change password`() {
        val passwordEncoder = MemberFixture.DEFAULT_PASSWORD_ENCODER
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()
        val newPassword = "newSecurePassword"

        val updatedMember = activatedMember.changePassword(newPassword, passwordEncoder)

        assertEquals(true, passwordEncoder.matches(newPassword, updatedMember.passwordHash))
    }

    @Test
    fun `test change password when not active`() {
        val passwordEncoder = MemberFixture.DEFAULT_PASSWORD_ENCODER
        val member = MemberFixture.createMember()

        assertFailsWith<IllegalArgumentException> {
            member.changePassword("newSecurePassword", passwordEncoder)
        }.let {
            assertEquals("등록 완료 상태에서만 비밀번호 변경이 가능합니다", it.message)
        }
    }

    @Test
    fun `test update info`() {
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
    }

    @Test
    fun `test update info when not active`() {
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
    fun `test update trust score`() {
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
    fun `test can write review`() {
        val member = MemberFixture.createMember()
        val activatedMember = member.activate()

        val canWriteReview = activatedMember.canWriteReview()

        assertEquals(true, canWriteReview)
    }

    @Test
    fun `test cannot write review when not active`() {
        val member = MemberFixture.createMember()

        val canWriteReview = member.canWriteReview()

        assertEquals(false, canWriteReview)
    }
}
