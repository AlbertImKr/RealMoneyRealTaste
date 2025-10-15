package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.domain.member.MemberStatus
import com.albert.realmoneyrealtaste.domain.member.exceptions.MemberNotFoundException
import com.albert.realmoneyrealtaste.util.MemberFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
}
