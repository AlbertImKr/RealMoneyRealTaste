package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.value.Email
import com.albert.realmoneyrealtaste.util.MemberFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberReaderTest(
    private val memberReader: MemberReader,
    private val memberRegister: MemberRegister,
) : IntegrationTestBase() {

    @Test
    fun `readMemberById - success - returns member when member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val member = memberReader.readMemberById(registeredMember.id!!)

        assertEquals(registeredMember.id, member.id)
        assertEquals(registeredMember.email, member.email)
        assertEquals(registeredMember.nickname, member.nickname)
        assertEquals(MemberStatus.PENDING, member.status)
    }

    @Test
    fun `readMemberById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.readMemberById(nonExistentId)
        }
    }

    @Test
    fun `readActiveMemberById - success - returns active member when active member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)
        registeredMember.activate()

        val member = memberReader.readActiveMemberById(registeredMember.id!!)

        assertEquals(registeredMember.id, member.id)
        assertEquals(MemberStatus.ACTIVE, member.status)
    }

    @Test
    fun `readActiveMemberById - failure - throws exception when member is not active`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        assertFailsWith<MemberNotFoundException> {
            memberReader.readActiveMemberById(registeredMember.id!!)
        }
    }

    @Test
    fun `readActiveMemberById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.readActiveMemberById(nonExistentId)
        }
    }

    @Test
    fun `readMemberByEmail - success - returns member when member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val member = memberReader.readMemberByEmail(MemberFixture.DEFAULT_EMAIL)

        assertEquals(registeredMember.id, member.id)
        assertEquals(registeredMember.email, member.email)
        assertEquals(registeredMember.nickname, member.nickname)
    }

    @Test
    fun `readMemberByEmail - failure - throws exception when member does not exist`() {
        val nonExistentEmail = Email("nonexistent@example.com")

        assertFailsWith<MemberNotFoundException> {
            memberReader.readMemberByEmail(nonExistentEmail)
        }
    }

    @Test
    fun `existsActiveMemberById - success - returns true when active member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)
        registeredMember.activate()

        val exists = memberReader.existsActiveMemberById(registeredMember.id!!)

        assertTrue(exists)
    }

    @Test
    fun `existsActiveMemberById - success - returns false when member is not active`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)

        val exists = memberReader.existsActiveMemberById(registeredMember.id!!)

        assertFalse(exists)
    }

    @Test
    fun `existsActiveMemberById - success - returns false when member does not exist`() {
        val nonExistentId = 99999L

        val exists = memberReader.existsActiveMemberById(nonExistentId)

        assertFalse(exists)
    }

    @Test
    fun `getNicknameById - success - returns nickname when member exists`() {
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = MemberFixture.DEFAULT_RAW_PASSWORD,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        val registeredMember = memberRegister.register(request)
        registeredMember.activate()

        val nickname = memberReader.getNicknameById(registeredMember.id!!)

        assertEquals(MemberFixture.DEFAULT_NICKNAME.value, nickname)
    }

    @Test
    fun `getNicknameById - failure - throws exception when member does not exist`() {
        val nonExistentId = 99999L

        assertFailsWith<MemberNotFoundException> {
            memberReader.getNicknameById(nonExistentId)
        }
    }
}
