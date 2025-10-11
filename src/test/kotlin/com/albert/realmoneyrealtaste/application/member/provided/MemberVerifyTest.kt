package com.albert.realmoneyrealtaste.application.member.provided

import com.albert.realmoneyrealtaste.IntegrationTestBase
import com.albert.realmoneyrealtaste.application.member.dto.MemberRegisterRequest
import com.albert.realmoneyrealtaste.application.member.exception.MemberNotFoundException
import com.albert.realmoneyrealtaste.domain.member.value.RawPassword
import com.albert.realmoneyrealtaste.util.MemberFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemberVerifyTest(
    val memberVerify: MemberVerify,
    val memberRegister: MemberRegister,
) : IntegrationTestBase() {

    @Test
    fun `verify - failure - throws MemberNotFoundException when member does not exist`() {
        assertFailsWith<MemberNotFoundException> {
            memberVerify.verify(
                email = MemberFixture.DEFAULT_EMAIL,
                password = MemberFixture.DEFAULT_RAW_PASSWORD
            )
        }
    }

    @Test
    fun `verify - failure - throws MemberNotFoundException when password is incorrect`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = MemberFixture.DEFAULT_NICKNAME
        )
        memberRegister.register(request)
        val wrongPassword = RawPassword("wrong${password.value}")

        assertFailsWith<MemberNotFoundException> {
            memberVerify.verify(
                email = email,
                password = wrongPassword
            )
        }
    }

    @Test
    fun `verify - success - returns MemberPrincipal when credentials are correct`() {
        val password = MemberFixture.DEFAULT_RAW_PASSWORD
        val email = MemberFixture.DEFAULT_EMAIL
        val nickname = MemberFixture.DEFAULT_NICKNAME
        val request = MemberRegisterRequest(
            email = email,
            password = password,
            nickname = nickname
        )
        memberRegister.register(request)

        val principal = memberVerify.verify(
            email = email,
            password = password
        )

        assertEquals(email, principal.email)
        assertEquals(nickname, principal.nickname)
    }
}
