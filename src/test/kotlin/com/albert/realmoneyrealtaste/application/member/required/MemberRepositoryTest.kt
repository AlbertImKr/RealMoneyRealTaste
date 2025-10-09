package com.albert.realmoneyrealtaste.application.member.required

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.domain.member.Email
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.Nickname
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemberRepositoryTest(
    val memberRepository: MemberRepository,
) : IntegrationTestBase() {

    @Test
    fun `save - success - saves and returns member`() {
        val member = MemberFixture.createMember()

        val savedMember = memberRepository.save(member)

        assertNotNull(savedMember)
        assertEquals(member.email, savedMember.email)
        assertEquals(member.nickname, savedMember.nickname)
        assertEquals(member.passwordHash, savedMember.passwordHash)
        assertEquals(member.status, savedMember.status)
    }

    @Test
    fun `save - success - assigns id to new member`() {
        val member = MemberFixture.createMember()

        val savedMember = memberRepository.save(member)

        assertNotNull(savedMember.id)
    }

    @Test
    fun `findByEmail - success - returns member when exists`() {
        val email = Email("find-by-email@example.com")
        val member = MemberFixture.createMember(email)
        memberRepository.save(member)
        flushAndClear()

        val foundMember = memberRepository.findByEmail(email)

        assertNotNull(foundMember)
        assertEquals(email, foundMember.email)
    }

    @Test
    fun `findByEmail - failure - returns null when member does not exist`() {
        val nonExistentEmail = Email("nonexistent@example.com")

        val foundMember = memberRepository.findByEmail(nonExistentEmail)

        assertNull(foundMember)
    }

    @Test
    fun `findById - success - returns member when exists`() {
        val member = MemberFixture.createMember()
        val savedMember = memberRepository.save(member)
        flushAndClear()

        val foundMember = memberRepository.findById(savedMember.id!!)

        assertNotNull(foundMember)
        assertEquals(savedMember.id, foundMember.id)
        assertEquals(savedMember.email, foundMember.email)
    }

    @Test
    fun `findById - failure - returns null when member does not exist`() {
        val nonExistentId = 999999L

        val foundMember = memberRepository.findById(nonExistentId)

        assertNull(foundMember)
    }

    @Test
    fun `save - success - updates existing member`() {
        val member = MemberFixture.createMember()
        val savedMember = memberRepository.save(member)
        flushAndClear()

        savedMember.activate()
        val updatedMember = memberRepository.save(savedMember)
        flushAndClear()

        val foundMember = memberRepository.findById(updatedMember.id!!)
        assertNotNull(foundMember)
        assertEquals(MemberStatus.ACTIVE, foundMember.status)
    }

    @Test
    fun `findByEmail - success - returns correct member when multiple members exist`() {
        val email1 = Email("member1@example.com")
        val nickname1 = Nickname("member1")
        val email2 = Email("member2@example.com")
        val nickname2 = Nickname("member2")
        memberRepository.save(MemberFixture.createMember(email = email1, nickname = nickname1))
        memberRepository.save(MemberFixture.createMember(email = email2, nickname = nickname2))
        flushAndClear()

        val foundMember = memberRepository.findByEmail(email1)

        assertNotNull(foundMember)
        assertEquals(email1, foundMember.email)
    }

    @Test
    fun `findById - success - returns correct member when multiple members exist`() {
        val member1 = memberRepository.save(
            MemberFixture.createMember(
                email = Email("id1@example.com"),
                nickname = Nickname("id1")
            )
        )
        memberRepository.save(MemberFixture.createMember(email = Email("id2@example.com"), nickname = Nickname("id2")))
        flushAndClear()

        val foundMember = memberRepository.findById(member1.id!!)

        assertNotNull(foundMember)
        assertEquals(member1.id, foundMember.id)
        assertEquals(member1.email, foundMember.email)
    }

    @Test
    fun `findByEmail - success - is case sensitive`() {
        val email = Email("CaseSensitive@example.com")
        memberRepository.save((MemberFixture.createMember(email = email)))
        flushAndClear()

        val foundWithSameCase = memberRepository.findByEmail(email)
        memberRepository.findByEmail(email)

        assertNotNull(foundWithSameCase)
    }
}
