package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.exception.DuplicateEmailException
import com.albert.realmoneyrealtaste.domain.member.MemberFixture
import com.albert.realmoneyrealtaste.domain.member.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemberRegisterTest(
    val memberRegister: MemberRegister,
    val passwordEncoder: PasswordEncoder,
) : IntegrationTestBase() {

    @Test
    fun `register - success`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME,
        )

        val member = memberRegister.register(request)

        assertEquals(request.email, member.email)
        assertEquals(request.nickname, member.nickname)
        assertEquals(true, member.verifyPassword(password, passwordEncoder))
    }

    @Test
    fun `register - duplicate email`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val request = MemberRegisterRequest(
            email = MemberFixture.DEFAULT_EMAIL,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME,
        )

        memberRegister.register(request)

        assertFailsWith<DuplicateEmailException>(
            message = "이미 사용 중인 이메일입니다."
        ) {
            memberRegister.register(request)
        }
    }
}
