package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateEmailException
import com.albert.realmoneyrealtaste.domain.member.service.PasswordEncoder
import com.albert.realmoneyrealtaste.util.MemberFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MemberRegisterTest(
    val memberRegister: MemberRegister,
    val passwordEncoder: PasswordEncoder,
) : IntegrationTestBase() {

    @Test
    fun `register - success - creates member with correct information`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )

        val member = memberRegister.register(request)

        assertEquals(request.email, member.email)
        assertEquals(request.nickname, member.nickname)
        assertTrue(member.verifyPassword(password, passwordEncoder))
    }

    @Test
    fun `register - failure - throws exception when email is duplicate`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )

        memberRegister.register(request)

        assertFailsWith<DuplicateEmailException> {
            memberRegister.register(request)
        }.let {
            assertEquals("이미 사용 중인 이메일입니다.", it.message)
        }
    }
}
